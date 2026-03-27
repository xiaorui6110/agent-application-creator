package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import com.xiaorui.agentapplicationcreator.enums.AppVersionSourceEnum;
import com.xiaorui.agentapplicationcreator.enums.CodeGenTypeEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.AppVersionMapper;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.AppVersion;
import com.xiaorui.agentapplicationcreator.model.vo.AppVersionVO;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.AppVersionService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author xiaorui
 */
@Slf4j
@Service
public class AppVersionServiceImpl extends ServiceImpl<AppVersionMapper, AppVersion> implements AppVersionService {

    @Lazy
    @Resource
    private AppService appService;

    @Resource
    private AppProperties appProperties;

    @Override
    public AppVersion createVersionSnapshot(String appId, String versionSource, String versionNote, String deployUrl) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "appId is blank");
        ThrowUtil.throwIf(StrUtil.isBlank(versionSource), ErrorCode.PARAMS_ERROR, "versionSource is blank");

        App app = appService.getById(appId);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "app not found");

        File sourceDir = appProperties.resolveCodeOutputAppDir(appId).toFile();
        ThrowUtil.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), ErrorCode.NOT_FOUND_ERROR, "app source directory not found");
        File[] children = sourceDir.listFiles();
        ThrowUtil.throwIf(children == null || children.length == 0, ErrorCode.NOT_FOUND_ERROR, "app source files not found");

        int nextVersionNumber = getNextVersionNumber(appId);
        String relativeSnapshotPath = appId + File.separator + "v" + nextVersionNumber;
        Path snapshotDir = appProperties.resolveVersionSnapshotDir(relativeSnapshotPath);

        try {
            File snapshotDirFile = snapshotDir.toFile();
            if (snapshotDirFile.exists()) {
                FileUtil.del(snapshotDirFile);
            }
            FileUtil.mkdir(snapshotDirFile);
            FileUtil.copyContent(sourceDir, snapshotDirFile, true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "failed to create app version snapshot: " + e.getMessage());
        }

        AppVersion appVersion = AppVersion.builder()
                .appId(appId)
                .versionNumber(nextVersionNumber)
                .versionSource(versionSource)
                .versionNote(StrUtil.blankToDefault(versionNote, buildDefaultVersionNote(versionSource, nextVersionNumber)))
                .snapshotPath(relativeSnapshotPath.replace(File.separatorChar, '/'))
                .entryFile(resolveEntryFile(app))
                .deployUrl(deployUrl)
                .createdBy(resolveCreatedBy(app))
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();

        boolean saved = this.save(appVersion);
        ThrowUtil.throwIf(!saved, ErrorCode.OPERATION_ERROR, "failed to save app version");
        return appVersion;
    }

    @Override
    public List<AppVersionVO> listAppVersions(String appId) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "appId is blank");
        return QueryChain.of(AppVersion.class)
                .eq(AppVersion::getAppId, appId)
                .eq(AppVersion::getIsDeleted, 0)
                .orderBy(AppVersion::getVersionNumber, false)
                .list()
                .stream()
                .map(item -> BeanUtil.copyProperties(item, AppVersionVO.class))
                .toList();
    }

    @Override
    public boolean restoreVersion(String appId, String appVersionId) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "appId is blank");
        ThrowUtil.throwIf(StrUtil.isBlank(appVersionId), ErrorCode.PARAMS_ERROR, "appVersionId is blank");

        AppVersion appVersion = QueryChain.of(AppVersion.class)
                .eq(AppVersion::getAppVersionId, appVersionId)
                .eq(AppVersion::getAppId, appId)
                .eq(AppVersion::getIsDeleted, 0)
                .one();
        ThrowUtil.throwIf(appVersion == null, ErrorCode.NOT_FOUND_ERROR, "app version not found");

        Path snapshotDir = appProperties.resolveVersionSnapshotDir(appVersion.getSnapshotPath());
        File snapshotDirFile = snapshotDir.toFile();
        ThrowUtil.throwIf(!snapshotDirFile.exists() || !snapshotDirFile.isDirectory(), ErrorCode.NOT_FOUND_ERROR, "snapshot directory not found");

        File targetDir = appProperties.resolveCodeOutputAppDir(appId).toFile();
        try {
            if (targetDir.exists()) {
                FileUtil.clean(targetDir);
            } else {
                FileUtil.mkdir(targetDir);
            }
            FileUtil.copyContent(snapshotDirFile, targetDir, true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "failed to restore app version: " + e.getMessage());
        }

        createVersionSnapshot(appId, AppVersionSourceEnum.RESTORED.getValue(),
                "restore from version #" + appVersion.getVersionNumber(), appVersion.getDeployUrl());
        return true;
    }

    private int getNextVersionNumber(String appId) {
        AppVersion latestVersion = QueryChain.of(AppVersion.class)
                .eq(AppVersion::getAppId, appId)
                .eq(AppVersion::getIsDeleted, 0)
                .orderBy(AppVersion::getVersionNumber, false)
                .limit(1)
                .one();
        return latestVersion == null || latestVersion.getVersionNumber() == null ? 1 : latestVersion.getVersionNumber() + 1;
    }

    private String resolveEntryFile(App app) {
        if (app != null && CodeGenTypeEnum.VUE_PROJECT.getValue().equals(app.getCodeGenType())) {
            return "dist/index.html";
        }
        return "index.html";
    }

    private String resolveCreatedBy(App app) {
        try {
            return SecurityUtil.getUserInfo().getUserId();
        } catch (Exception e) {
            return app == null ? null : app.getUserId();
        }
    }

    private String buildDefaultVersionNote(String versionSource, int versionNumber) {
        if (AppVersionSourceEnum.DEPLOYED.getValue().equals(versionSource)) {
            return "deploy snapshot v" + versionNumber;
        }
        if (AppVersionSourceEnum.RESTORED.getValue().equals(versionSource)) {
            return "restore snapshot v" + versionNumber;
        }
        return "generated snapshot v" + versionNumber;
    }
}
