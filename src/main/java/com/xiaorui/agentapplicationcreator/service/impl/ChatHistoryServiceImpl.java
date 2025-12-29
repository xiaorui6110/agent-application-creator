package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.enums.ChatHistoryMsgTypeEnum;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.ChatHistoryMapper;
import com.xiaorui.agentapplicationcreator.model.dto.chathistory.ChatHistoryQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.ChatHistory;
import com.xiaorui.agentapplicationcreator.service.ChatHistoryService;
import com.xiaorui.agentapplicationcreator.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.xiaorui.agentapplicationcreator.constants.RedisMemoryConstant.AI_MESSAGE_MEMORY_PREFIX;
import static com.xiaorui.agentapplicationcreator.constants.RedisMemoryConstant.USER_MESSAGE_MEMORY_PREFIX;

/**
 * 对话历史表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{


    /**
     * 保存对话历史
     *
     * @param appId 应用id
     * @param userId 用户id
     * @param parentId 父消息id
     * @param chatMessage 对话消息
     * @param chatMessageType 消息类型：user/ai
     * @return true/false
     */
    @Override
    public boolean saveChatHistory(String appId, String userId, String parentId, String chatMessage, String chatMessageType) {
        // 参数校验
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(userId) || StrUtil.isBlank(chatMessage) || StrUtil.isBlank(chatMessageType)) {
            throw new IllegalArgumentException("参数不能为空");
        }
        ChatHistoryMsgTypeEnum messageTypeEnum = ChatHistoryMsgTypeEnum.getEnumByValue(chatMessageType);
        ThrowUtil.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型");
        // 保存对话历史
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .parentId(parentId)
                .chatMessage(chatMessage)
                .chatMessageType(chatMessageType)
                .build();
        return this.save(chatHistory);
    }

    /**
     * 获取查询条件
     *
     * @param chatHistoryQueryRequest 对话历史查询请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        // 参数校验
        ThrowUtil.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String chatHistoryId = chatHistoryQueryRequest.getChatHistoryId();
        String appId = chatHistoryQueryRequest.getAppId();
        String userId = chatHistoryQueryRequest.getUserId();
        String parentId = chatHistoryQueryRequest.getParentId();
        String chatMessage = chatHistoryQueryRequest.getChatMessage();
        String chatMessageType = chatHistoryQueryRequest.getChatMessageType();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("chat_history_id", chatHistoryId)
                .eq("app_id", appId)
                .eq("user_id", userId)
                .eq("parent_id", parentId)
                .like("chat_message", chatMessage)
                .eq("chat_message_type", chatMessageType);
        // 游标查询逻辑 - 只使用 create_time 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("create_time", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("create_time", false);
        }
        return queryWrapper;
    }

    /**
     * 加载对话历史到 Redis 内存
     *
     * @param appId 应用id
     * @param chatMessage 对话消息
     * @param maxCount 最大数量
     * @return 加载的对话历史数量
     */
    @Override
    public int loadChatHistoryToRedis(String appId, String chatMessage, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载（可以传一个值 或多个）
            RedisUtil.del();
            for (ChatHistory history : historyList) {
                if (ChatHistoryMsgTypeEnum.USER.getValue().equals(history.getChatMessageType())) {
                    RedisUtil.set(USER_MESSAGE_MEMORY_PREFIX, history.getChatMessage());
                    loadedCount++;
                } else if (ChatHistoryMsgTypeEnum.AI.getValue().equals(history.getChatMessageType())) {
                    RedisUtil.set(AI_MESSAGE_MEMORY_PREFIX, history.getChatMessage());
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }

    }
}
