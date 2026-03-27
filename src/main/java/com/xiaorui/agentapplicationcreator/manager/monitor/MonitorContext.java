package com.xiaorui.agentapplicationcreator.manager.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 监控上下文
 * @author: xiaorui
 * @date: 2026-01-23 10:18
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 3119758750913185989L;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 线程id
     */
    private String threadId;

    /**
     * 模型名称
     */
    private String agentName;

}
