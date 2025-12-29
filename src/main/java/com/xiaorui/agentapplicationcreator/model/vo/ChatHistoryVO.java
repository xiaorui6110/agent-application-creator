package com.xiaorui.agentapplicationcreator.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description: 对话历史vo
 * @author: xiaorui
 * @date: 2025-12-29 13:39
 **/
@Data
public class ChatHistoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1621108855117893942L;

    /**
     * 对话历史id（雪花算法）
     */
    private String chatHistoryId;

    /**
     * 对话消息
     */
    private String chatMessage;

    /**
     * 消息类型：user/ai
     */
    private String chatMessageType;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 创建用户id
     */
    private String userId;

    /**
     * 父消息id（用于上下文关联）
     */
    private String parentId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
