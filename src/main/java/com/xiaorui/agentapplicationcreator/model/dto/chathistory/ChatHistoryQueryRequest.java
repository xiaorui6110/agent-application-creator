package com.xiaorui.agentapplicationcreator.model.dto.chathistory;

import com.xiaorui.agentapplicationcreator.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description: 对话信息查询请求
 * @author: xiaorui
 * @date: 2025-12-29 13:49
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3991984324574455260L;

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
     * 游标查询 - 最后一条记录的创建时间
     * 用于分页查询，获取早于此时间的记录
     */
    private LocalDateTime lastCreateTime;

}