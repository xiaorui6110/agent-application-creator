package com.xiaorui.agentapplicationcreator.ai.model.result;

import lombok.Data;

/**
 * @description: 单 HTML 代码生成结果
 * @author: xiaorui
 * @date: 2025-12-18 13:57
 **/
@Data
public class SingleFileCodeResult {

    /**
     *  HTML 代码
     */
    private String htmlCode;

    /**
     * 描述
     */
    private String description;

}
