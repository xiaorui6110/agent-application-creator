package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.enums.ChatHistoryMsgTypeEnum;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.ChatHistoryMapper;
import com.xiaorui.agentapplicationcreator.model.dto.chathistory.ChatHistoryQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.ChatHistory;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.ChatHistoryService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 保存对话历史（保存到 MySQL 数据库）
     *
     * @param appId 应用id
     * @param userId 用户id
     * @param chatMessage 对话消息
     * @param chatMessageType 消息类型：user/ai
     * @return true/false
     */
    @Override
    public boolean saveChatHistory(String appId, String userId, String chatMessage, String chatMessageType) {
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
                .chatMessage(chatMessage)
                .chatMessageType(chatMessageType)
                .build();
        return this.save(chatHistory);
    }

    /**
     * 获取查询条件 （设想是使用mongodb 来进行对话历史的查询的，添加个MySQL查询也行）
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
        String chatMessage = chatHistoryQueryRequest.getChatMessage();
        ChatHistoryMsgTypeEnum chatMessageType = ChatHistoryMsgTypeEnum
                .getEnumByValue(chatHistoryQueryRequest.getChatMessageType());
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("chat_history_id", chatHistoryId)
                .eq("app_id", appId)
                .eq("user_id", userId)
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

    @Override
    public List<ChatHistory> listRecentChatHistory(String appId, int limit) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtil.throwIf(limit <= 0 || limit > 100, ErrorCode.PARAMS_ERROR, "查询数量必须在1~100之间");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("app_id", appId)
                .orderBy("create_time", false)
                .limit(limit);
        List<ChatHistory> historyList = this.mapper.selectListByQuery(queryWrapper);
        if (CollUtil.isEmpty(historyList)) {
            return List.of();
        }
        return historyList.reversed();
    }

    /**
     * 兼容旧接口。
     * 当前运行态对话上下文由 Agent 框架的 RedisSaver 负责，
     * MySQL 才是业务历史主存。该方法不再向自定义 Redis key 写入数据。
     *
     * @param appId 应用id
     * @param maxCount 最大数量
     * @return 加载的对话历史数量
     */
    @Override
    @Deprecated
    public int loadChatHistoryToRedis(String appId, int maxCount) {
        try {
            List<ChatHistory> historyList = listRecentChatHistory(appId, maxCount);
            log.warn("loadChatHistoryToRedis() 已降级为兼容接口，当前仅从 MySQL 读取最近历史数量，appId={}, count={}",
                    appId, historyList.size());
            return historyList.size();
        } catch (Exception e) {
            log.error("读取历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 分页查询应用的对话历史
     *
     * @param appId 应用id
     * @param pageSize 每页数量
     * @param lastCreateTime 上一次查询的时间
     * @return 对话历史列表
     */
    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(String appId, int pageSize, LocalDateTime lastCreateTime) {
        // 参数校验
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtil.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1~50之间");
        App app = appService.getById(appId);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 判断是否是创建者或者是否为管理员
        String userId = SecurityUtil.getUserInfo().getUserId();
        boolean isCreator = app.getUserId().equals(userId);
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(userService.getById(userId).getUserRole());
        ThrowUtil.throwIf(!isAdmin && !isCreator, ErrorCode.NOT_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    /**
     * 根据应用id删除对话历史（主要是用于关联删除）
     *
     * @param appId 应用id
     * @return true/false
     */
    @Override
    public boolean deleteByAppId(String appId) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "应用id不能为空");
        return this.remove(QueryWrapper.create().eq("appId", appId));
    }
}
