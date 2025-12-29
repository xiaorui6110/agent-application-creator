package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.dto.chathistory.ChatHistoryQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.ChatHistory;

/**
 * 对话历史表 服务层。
 *
 * @author xiaorui
 */
public interface ChatHistoryService extends IService<ChatHistory> {

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
    boolean saveChatHistory(String appId, String userId, String parentId, String chatMessage, String chatMessageType);

    /**
     * 获取查询条件（通过appId、userId、parentId、chatMessage、chatMessageType查询）
     *
     * @param chatHistoryQueryRequest 对话历史查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 加载对话历史到 Redis 内存
     *
     * @param appId 应用id
     * @param chatMessage 对话消息
     * @param maxCount 最大数量
     * @return 加载的对话历史数量
     */
    int loadChatHistoryToRedis(String appId, String chatMessage, int maxCount);

}
