package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import com.xiaorui.agentapplicationcreator.enums.CodeGenTypeEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.AppMapper;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.AppVO;
import com.xiaorui.agentapplicationcreator.model.vo.UserVO;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.ScreenshotService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import com.xiaorui.agentapplicationcreator.util.SftpFileUtil;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.xiaorui.agentapplicationcreator.constant.AppConstant.GOOD_APP_PRIORITY;

@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private static final int MAX_INPUT_LENGTH = 2000;

    @Resource
    private UserService userService;

    @Resource
    private SftpFileUtil sftpFileUtil;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AppProperties appProperties;

    @Override
    public String createApp(String appInitPrompt) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "user not found");
        }

        ThrowUtil.throwIf(StrUtil.isBlank(appInitPrompt), ErrorCode.PARAMS_ERROR, "appInitPrompt is blank");
        validateUserInput(appInitPrompt);

        App app = new App();
        app.setAppName(appInitPrompt.substring(0, Math.min(appInitPrompt.length(), 12)));
        app.setUserId(userId);
        app.setAppInitPrompt(appInitPrompt);
        app.setAppCover("https://picsum.photos/1200/600");
        app.setDeployKey(RandomUtil.randomString(6));

        boolean result = this.save(app);
        ThrowUtil.throwIf(!result, ErrorCode.OPERATION_ERROR, "failed to create app");
        log.info("app create success, appId: {}, userId: {}", app.getAppId(), userId);
        return app.getAppId();
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        ThrowUtil.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        String appId = appQueryRequest.getAppId();
        String appName = appQueryRequest.getAppName();
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.getEnumByValue(appQueryRequest.getCodeGenType());
        return QueryWrapper.create()
                .eq("app_id", appId)
                .like("app_name", appName)
                .eq("code_gen_type", codeGenType)
                .orderBy("app_name");
    }

    @Override
    public String deployApp(String appId) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "user not found");
        }

        App app = this.mapper.selectOneById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "app not found");
        }
        if (!app.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "no access to deploy this app");
        }

        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }

        File sourceDir = appProperties.resolveCodeOutputAppDir(appId).toFile();
        validateDeploySource(app, sourceDir);

        File deployDir = appProperties.resolveCodeDeployAppDir(deployKey).toFile();
        try {
            FileUtil.copyContent(sourceDir, deployDir, true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "failed to copy deploy files: " + e.getMessage());
        }

        String deployToLinuxDirPath = appProperties.getDeploy().getRemoteDir();
        try {
            sftpFileUtil.uploadDirToLinux(deployDir.getAbsolutePath(), deployToLinuxDirPath);
            log.info("upload dir to linux success, remoteDir: {}", deployToLinuxDirPath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "failed to upload deploy files: " + e.getMessage());
        }

        App updateApp = new App();
        updateApp.setAppId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        updateApp.setUpdateTime(LocalDateTime.now());
        updateApp.setDeployUrl(appProperties.buildDeployUrl(deployKey));
        boolean updateResult = this.updateById(updateApp);
        ThrowUtil.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "failed to update deploy info");

        String appDeployUrl = appProperties.buildDeployUrl(deployKey);
        createAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;
    }

    @Override
    public AppVO getAppInfo(String appId) {
        App app = this.mapper.selectOneById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "app not found");
        }
        return buildAppVO(app, loadUserInfoMap(Collections.singletonList(app)));
    }

    @Override
    public List<AppVO> getAppInfoList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        return buildAppVOList(appList);
    }

    @Override
    public List<AppVO> getMyAppInfoList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        String userId = SecurityUtil.getUserInfo().getUserId();
        List<App> filteredApps = appList.stream()
                .filter(app -> userId.equals(app.getUserId()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(filteredApps)) {
            return new ArrayList<>();
        }
        return buildAppVOList(filteredApps);
    }

    @Override
    public List<AppVO> getAppInfoListForGoods(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        List<App> featuredApps = appList.stream()
                .filter(app -> GOOD_APP_PRIORITY.equals(app.getAppPriority()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(featuredApps)) {
            return new ArrayList<>();
        }
        return buildAppVOList(featuredApps);
    }

    @Override
    public void createAppScreenshotAsync(String appId, String appDeploy) {
        Thread.startVirtualThread(() -> {
            String screenshotUrl = screenshotService.createAndUploadScreenshot(appDeploy);
            App updateApp = new App();
            updateApp.setAppId(appId);
            updateApp.setAppCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtil.throwIf(!updated, ErrorCode.OPERATION_ERROR, "failed to update app cover");
        });
    }

    @Override
    public void updateAppNameAsync(String appId, String appName) {
        Thread.startVirtualThread(() -> {
            try {
                App updateApp = new App();
                updateApp.setAppId(appId);
                updateApp.setAppName(appName);
                boolean updated = this.updateById(updateApp);
                ThrowUtil.throwIf(!updated, ErrorCode.OPERATION_ERROR, "failed to update app name");
            } catch (Exception e) {
                log.error("failed to update app name asynchronously, appId: {}", appId, e);
            }
        });
    }

    @Override
    public void updateAppCodeGenTypeAsync(String appId, String codeGenType) {
        Thread.startVirtualThread(() -> {
            try {
                App updateApp = new App();
                updateApp.setAppId(appId);
                updateApp.setCodeGenType(codeGenType);
                boolean updated = this.updateById(updateApp);
                ThrowUtil.throwIf(!updated, ErrorCode.OPERATION_ERROR, "failed to update codeGenType");
            } catch (Exception e) {
                log.error("failed to update codeGenType asynchronously, appId: {}", appId, e);
            }
        });
    }

    private List<AppVO> buildAppVOList(List<App> appList) {
        Map<String, UserVO> userInfoMap = loadUserInfoMap(appList);
        return appList.stream()
                .map(app -> buildAppVO(app, userInfoMap))
                .collect(Collectors.toList());
    }

    private AppVO buildAppVO(App app, Map<String, UserVO> userInfoMap) {
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        if (userInfoMap != null) {
            appVO.setUserVO(userInfoMap.get(app.getUserId()));
        }
        return appVO;
    }

    private Map<String, UserVO> loadUserInfoMap(List<App> appList) {
        Set<String> userIds = appList.stream()
                .map(App::getUserId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, userService::getUserInfo));
    }

    private void validateDeploySource(App app, File sourceDir) {
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "app code directory not found");
        }
        File[] children = sourceDir.listFiles();
        if (children == null || children.length == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "app code directory is empty");
        }
        File entryFile = resolveDeployEntryFile(app, sourceDir);
        if (!entryFile.exists() || !entryFile.isFile()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "deploy entry file not found: " + entryFile.getName());
        }
    }

    private File resolveDeployEntryFile(App app, File sourceDir) {
        if (app != null && CodeGenTypeEnum.VUE_PROJECT.getValue().equals(app.getCodeGenType())) {
            return new File(new File(sourceDir, "dist"), "index.html");
        }
        return new File(sourceDir, "index.html");
    }

    private void validateUserInput(String input) {
        if (StringUtil.isBlank(input)) {
            throw new BusinessException("input is blank", ErrorCode.PARAMS_ERROR);
        }
        if (input.length() > MAX_INPUT_LENGTH) {
            throw new BusinessException("input is too long", ErrorCode.PARAMS_ERROR);
        }
        if (SensitiveWordHelper.contains(input)) {
            throw new BusinessException("input contains sensitive content", ErrorCode.PARAMS_ERROR);
        }
    }
}
