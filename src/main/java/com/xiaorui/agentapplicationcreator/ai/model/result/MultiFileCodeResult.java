package com.xiaorui.agentapplicationcreator.ai.model.result;

import lombok.Data;

/**
 * @description: 多文件代码生成结果
 * @author: xiaorui
 * @date: 2025-12-18 13:57
 **/
@Data
public class MultiFileCodeResult {

    /**
     * html 代码
     */
    private String htmlCode;

    /**
     * css 代码
     */
    private String cssCode;

    /**
     * js 代码
     */
    private String jsCode;

    /**
     * 描述
     */
    private String description;

}
