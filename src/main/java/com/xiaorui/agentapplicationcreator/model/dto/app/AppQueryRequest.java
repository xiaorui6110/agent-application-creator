package com.xiaorui.agentapplicationcreator.model.dto.app;

import com.xiaorui.agentapplicationcreator.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 应用查询请求
 * @author: xiaorui
 * @date: 2025-12-22 11:11
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8008136519720523420L;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 代码生成类型（枚举）
     */
    private String codeGenType;


}
