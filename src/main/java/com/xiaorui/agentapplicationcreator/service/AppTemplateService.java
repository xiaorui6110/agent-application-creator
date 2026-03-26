package com.xiaorui.agentapplicationcreator.service;

import com.xiaorui.agentapplicationcreator.model.vo.AppTemplateVO;

import java.util.List;

public interface AppTemplateService {

    AppTemplateVO createTemplateFromApp(String appId, String templateName, String templateDescription);

    List<AppTemplateVO> listTemplates();

    String createAppFromTemplate(String templateId, String appName, String appDescription);
}
