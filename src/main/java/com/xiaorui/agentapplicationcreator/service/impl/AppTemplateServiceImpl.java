package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import com.xiaorui.agentapplicationcreator.enums.AppVersionSourceEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.AppTemplateMapper;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.AppTemplate;
import com.xiaorui.agentapplicationcreator.model.vo.AppTemplateVO;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.AppTemplateService;
import com.xiaorui.agentapplicationcreator.service.AppVersionService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author xiaorui
 */
@Service
public class AppTemplateServiceImpl extends ServiceImpl<AppTemplateMapper, AppTemplate> implements AppTemplateService {

    private static final String TEMPLATE_FILES_DIR = "files";

    @Resource
    private AppService appService;

    @Resource
    private AppVersionService appVersionService;

    @Resource
    private AppProperties appProperties;

    @Override
    public AppTemplateVO createTemplateFromApp(String appId, String templateName, String templateDescription) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "appId is blank");
        ThrowUtil.throwIf(StrUtil.isBlank(templateName), ErrorCode.PARAMS_ERROR, "templateName is blank");

        App app = appService.getById(appId);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "app not found");

        String currentUserId = resolveCurrentUserId();
        ThrowUtil.throwIf(!currentUserId.equals(app.getUserId()), ErrorCode.NOT_AUTH_ERROR,
                "no access to create template from this app");

        File sourceDir = appProperties.resolveCodeOutputAppDir(appId).toFile();
        ThrowUtil.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), ErrorCode.NOT_FOUND_ERROR,
                "app source directory not found");

        AppTemplate appTemplate = AppTemplate.builder()
                .templateName(templateName)
                .templateDescription(templateDescription)
                .codeGenType(app.getCodeGenType())
                .entryFile(resolveEntryFile(app))
                .sourceAppId(appId)
                .createdBy(currentUserId)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        boolean saved = this.save(appTemplate);
        ThrowUtil.throwIf(!saved, ErrorCode.OPERATION_ERROR, "failed to save app template");

        Path templateRoot = appProperties.resolveTemplateDir(appTemplate.getTemplateId());
        File templateRootFile = templateRoot.toFile();
        File templateFilesDir = templateRoot.resolve(TEMPLATE_FILES_DIR).toFile();
        FileUtil.mkdir(templateFilesDir);
        FileUtil.copyContent(sourceDir, templateFilesDir, true);

        AppTemplate updateTemplate = new AppTemplate();
        updateTemplate.setTemplateId(appTemplate.getTemplateId());
        updateTemplate.setStoragePath(appTemplate.getTemplateId());
        updateTemplate.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(updateTemplate);
        ThrowUtil.throwIf(!updated, ErrorCode.OPERATION_ERROR, "failed to update app template storage path");

        appTemplate.setStoragePath(appTemplate.getTemplateId());
        return toVO(appTemplate);
    }

    @Override
    public List<AppTemplateVO> listTemplates() {
        return QueryChain.of(AppTemplate.class)
                .eq(AppTemplate::getIsDeleted, 0)
                .orderBy(AppTemplate::getCreateTime, false)
                .list()
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public String createAppFromTemplate(String templateId, String appName, String appDescription) {
        ThrowUtil.throwIf(StrUtil.isBlank(templateId), ErrorCode.PARAMS_ERROR, "templateId is blank");

        AppTemplate appTemplate = QueryChain.of(AppTemplate.class)
                .eq(AppTemplate::getTemplateId, templateId)
                .eq(AppTemplate::getIsDeleted, 0)
                .one();
        ThrowUtil.throwIf(appTemplate == null, ErrorCode.NOT_FOUND_ERROR, "template not found");

        Path templateRoot = appProperties.resolveTemplateDir(resolveTemplateStoragePath(appTemplate));
        File templateFilesDir = templateRoot.resolve(TEMPLATE_FILES_DIR).toFile();
        ThrowUtil.throwIf(!templateFilesDir.exists() || !templateFilesDir.isDirectory(), ErrorCode.NOT_FOUND_ERROR,
                "template files not found");

        String initPrompt = "create from template: " + appTemplate.getTemplateName();
        String appId = appService.createApp(initPrompt);
        App app = new App();
        app.setAppId(appId);
        app.setAppName(StrUtil.blankToDefault(appName, appTemplate.getTemplateName()));
        app.setAppDescription(StrUtil.blankToDefault(appDescription, appTemplate.getTemplateDescription()));
        app.setCodeGenType(appTemplate.getCodeGenType());
        app.setUpdateTime(LocalDateTime.now());
        boolean updated = appService.updateById(app);
        ThrowUtil.throwIf(!updated, ErrorCode.OPERATION_ERROR, "failed to initialize app from template");

        File targetDir = appProperties.resolveCodeOutputAppDir(appId).toFile();
        if (targetDir.exists()) {
            FileUtil.clean(targetDir);
        } else {
            FileUtil.mkdir(targetDir);
        }
        FileUtil.copyContent(templateFilesDir, targetDir, true);
        appVersionService.createVersionSnapshot(appId, AppVersionSourceEnum.GENERATED.getValue(),
                "generated from template " + appTemplate.getTemplateName(), null);
        return appId;
    }

    private AppTemplateVO toVO(AppTemplate appTemplate) {
        AppTemplateVO appTemplateVO = BeanUtil.copyProperties(appTemplate, AppTemplateVO.class);
        appTemplateVO.setCreatedTime(appTemplate.getCreateTime());
        return appTemplateVO;
    }

    private String resolveEntryFile(App app) {
        if (app == null) {
            return "index.html";
        }
        if ("vue_project".equals(app.getCodeGenType())) {
            return "dist/index.html";
        }
        return "index.html";
    }

    private String resolveCurrentUserId() {
        try {
            return SecurityUtil.getUserInfo().getUserId();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "user not logged in");
        }
    }

    private String resolveTemplateStoragePath(AppTemplate appTemplate) {
        String storagePath = appTemplate.getStoragePath();
        if (StrUtil.isBlank(storagePath)) {
            return appTemplate.getTemplateId();
        }
        return storagePath;
    }
}
