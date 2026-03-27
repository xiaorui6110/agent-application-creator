package com.xiaorui.agentapplicationcreator.service;

import com.xiaorui.agentapplicationcreator.model.vo.AppTemplateVO;

import java.util.List;

/**
 * @author xiaorui
 */
public interface AppTemplateService {

    /**
     * 从应用创建模板
     * @param appId 应用ID
     * @param templateName 模板名称
     * @param templateDescription 模板描述
     * @return 模板ID
     */
    AppTemplateVO createTemplateFromApp(String appId, String templateName, String templateDescription);

    /**
     * 列出所有模板
     * @return 模板列表
     */
    List<AppTemplateVO> listTemplates();

    /**
     * 创建应用从模板
     * @param templateId 模板ID
     * @param appName 应用名称
     * @param appDescription 应用描述
     * @return 应用ID
     */
    String createAppFromTemplate(String templateId, String appName, String appDescription);
}
