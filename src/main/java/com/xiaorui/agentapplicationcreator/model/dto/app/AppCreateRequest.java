package com.xiaorui.agentapplicationcreator.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 应用创建请求
 * @author: xiaorui
 * @date: 2025-12-22 11:10
 **/
@Data
public class AppCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -1650268905139268325L;

    /**
     * 应用初始化的 prompt
     */
    private String appInitPrompt;

}
