package com.xiaorui.agentapplicationcreator.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xiaorui.agentapplicationcreator.common.DeleteRequest;
import com.xiaorui.agentapplicationcreator.constant.AppConstant;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppAdminUpdateInfoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppCreateRequest;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppDeployRequest;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppTemplateCreateRequest;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppTemplateUseRequest;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppQueryRequest;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppUpdateInfoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppVersionRestoreRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.vo.AppVO;
import com.xiaorui.agentapplicationcreator.model.vo.AppTemplateVO;
import com.xiaorui.agentapplicationcreator.model.vo.AppVersionVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.AppTemplateService;
import com.xiaorui.agentapplicationcreator.service.AppVersionService;
import com.xiaorui.agentapplicationcreator.service.ProjectDownloadService;
import com.xiaorui.agentapplicationcreator.util.RedisCacheUtil;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaorui
 */
@RestController
@RequestMapping("/app")
public class AppController {

    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 50L;
    private static final long MAX_ADMIN_PAGE_SIZE = 100L;

    @Resource
    private AppService appService;

    @Resource
    private ProjectDownloadService projectDownloadService;

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Resource
    private AppVersionService appVersionService;

    @Resource
    private AppTemplateService appTemplateService;

    @PostMapping("/create")
    @Operation(summary = "create app", description = "create app")
    @Parameter(name = "appCreateRequest", description = "app create request")
    public ServerResponseEntity<String> createApp(@RequestBody AppCreateRequest appCreateRequest) {
        ThrowUtil.throwIf(appCreateRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        String appId = appService.createApp(appCreateRequest.getAppInitPrompt());
        return ServerResponseEntity.success(appId);
    }

    @GetMapping("/template/list")
    @Operation(summary = "list app templates", description = "list app templates")
    public ServerResponseEntity<List<AppTemplateVO>> listTemplates() {
        return ServerResponseEntity.success(appTemplateService.listTemplates());
    }

    @PostMapping("/template/create")
    @Operation(summary = "create app template", description = "create app template")
    @Parameter(name = "appTemplateCreateRequest", description = "app template create request")
    public ServerResponseEntity<AppTemplateVO> createTemplate(@RequestBody AppTemplateCreateRequest appTemplateCreateRequest) {
        ThrowUtil.throwIf(appTemplateCreateRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        return ServerResponseEntity.success(appTemplateService.createTemplateFromApp(
                appTemplateCreateRequest.getAppId(),
                appTemplateCreateRequest.getTemplateName(),
                appTemplateCreateRequest.getTemplateDescription()
        ));
    }

    @PostMapping("/template/use")
    @Operation(summary = "create app from template", description = "create app from template")
    @Parameter(name = "appTemplateUseRequest", description = "app template use request")
    public ServerResponseEntity<String> createAppFromTemplate(@RequestBody AppTemplateUseRequest appTemplateUseRequest) {
        ThrowUtil.throwIf(appTemplateUseRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        return ServerResponseEntity.success(appTemplateService.createAppFromTemplate(
                appTemplateUseRequest.getTemplateId(),
                appTemplateUseRequest.getAppName(),
                appTemplateUseRequest.getAppDescription()
        ));
    }

    @PostMapping("/deploy")
    @Operation(summary = "deploy app", description = "deploy app")
    @Parameter(name = "appDeployRequest", description = "app deploy request")
    public ServerResponseEntity<String> deployApp(@RequestBody AppDeployRequest appDeployRequest) {
        ThrowUtil.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        String deployUrl = appService.deployApp(appDeployRequest.getAppId());
        return ServerResponseEntity.success(deployUrl);
    }

    @PostMapping("/update")
    @Operation(summary = "update app", description = "update app")
    @Parameter(name = "appUpdateRequest", description = "app update request")
    public ServerResponseEntity<Boolean> updateApp(@RequestBody AppUpdateInfoRequest appUpdateRequest) {
        ThrowUtil.throwIf(appUpdateRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        String appId = appUpdateRequest.getAppId();
        appService.validateAppAccess(appId);

        App app = new App();
        app.setAppId(appId);
        app.setAppName(appUpdateRequest.getAppName());
        app.setAppCover(appUpdateRequest.getAppCover());
        app.setAppDescription(appUpdateRequest.getAppDescription());
        app.setUpdateTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtil.throwIf(!result, ErrorCode.OPERATION_ERROR, "failed to update app");
        return ServerResponseEntity.success(true);
    }

    @GetMapping("/get/info/{appId}")
    @Operation(summary = "get app info", description = "get app info")
    @Parameter(name = "appId", description = "app id")
    public ServerResponseEntity<AppVO> getAppInfoById(@PathVariable String appId) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "appId is blank");
        App app = appService.getById(appId);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "app not found");
        return ServerResponseEntity.success(appService.getAppInfo(appId));
    }

    @GetMapping("/category/list")
    @Operation(summary = "list app categories", description = "list app categories")
    public ServerResponseEntity<List<String>> listAppCategories() {
        return ServerResponseEntity.success(appService.listAppCategories());
    }

    @PostMapping("/recommend/list/page")
    @Operation(summary = "list recommended apps", description = "list recommended apps")
    @Parameter(name = "appQueryRequest", description = "app query request")
    public ServerResponseEntity<Page<AppVO>> listRecommendedApps(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtil.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        normalizePageRequest(appQueryRequest, MAX_PAGE_SIZE);
        return ServerResponseEntity.success(appService.listRecommendedApps(appQueryRequest));
    }

    @PostMapping("/rank/list/page")
    @Operation(summary = "list ranked apps", description = "list ranked apps")
    @Parameter(name = "appQueryRequest", description = "app query request")
    public ServerResponseEntity<Page<AppVO>> listRankedApps(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtil.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        normalizePageRequest(appQueryRequest, MAX_PAGE_SIZE);
        return ServerResponseEntity.success(appService.listRankedApps(appQueryRequest));
    }

    @PostMapping("/delete")
    @Operation(summary = "delete app", description = "delete app")
    @Parameter(name = "deleteRequest", description = "delete request")
    public ServerResponseEntity<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtil.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        String appId = deleteRequest.getId();
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "appId is blank");

        appService.validateAppAccess(appId);
        return ServerResponseEntity.success(appService.removeById(appId));
    }

    @PostMapping("/list/page/info")
    @Operation(summary = "list app page", description = "list app page")
    @Parameter(name = "appQueryRequest", description = "app query request")
    public ServerResponseEntity<Page<AppVO>> listAppInfoByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtil.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        normalizePageRequest(appQueryRequest, MAX_PAGE_SIZE);
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        String currentUserId = SecurityUtil.getUserInfo().getUserId();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        queryWrapper.eq("user_id", currentUserId);
        Page<App> appPage = appService.page(Page.of(current, pageSize), queryWrapper);

        Page<AppVO> appInfoPage = new Page<>(current, pageSize, appPage.getTotalRow());
        appInfoPage.setRecords(appService.getPrivateAppInfoList(appPage.getRecords()));
        return ServerResponseEntity.success(appInfoPage);
    }

    @PostMapping("/good/list/page/info")
    @Operation(summary = "list featured app page", description = "list featured app page")
    @Parameter(name = "appQueryRequest", description = "app query request")
    public ServerResponseEntity<Page<AppVO>> listGoodAppInfoByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtil.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        normalizePageRequest(appQueryRequest, 20L);
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();

        String category = StrUtil.blankToDefault(appQueryRequest.getAppCategory(), "all");
        String cacheKey = "good_app:list:" + category + ":" + current + ":" + pageSize;
        List<AppVO> cachedList = redisCacheUtil.get(cacheKey + ":data");
        Object cachedTotalObj = redisCacheUtil.get(cacheKey + ":total");
        Long cachedTotal = castToLong(cachedTotalObj);
        if (cachedList != null && cachedTotal != null) {
            Page<AppVO> appInfoPage = new Page<>(current, pageSize, cachedTotal);
            appInfoPage.setRecords(cachedList);
            return ServerResponseEntity.success(appInfoPage);
        }

        appQueryRequest.setAppPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(current, pageSize), queryWrapper);

        Page<AppVO> appInfoPage = new Page<>(current, pageSize, appPage.getTotalRow());
        List<AppVO> appInfoList = appService.getAppInfoListForGoods(appPage.getRecords());
        appInfoPage.setRecords(appInfoList);

        redisCacheUtil.set(cacheKey + ":data", appInfoList, 5, TimeUnit.MINUTES);
        redisCacheUtil.set(cacheKey + ":total", appPage.getTotalRow(), 5, TimeUnit.MINUTES);
        return ServerResponseEntity.success(appInfoPage);
    }

    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "admin delete app", description = "admin delete app")
    @Parameter(name = "deleteRequest", description = "delete request")
    public ServerResponseEntity<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtil.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        String appId = deleteRequest.getId();
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "appId is blank");
        App oldApp = appService.getById(appId);
        ThrowUtil.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "app not found");
        return ServerResponseEntity.success(appService.removeById(appId));
    }

    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "admin update app", description = "admin update app")
    @Parameter(name = "appAdminUpdateRequest", description = "app admin update request")
    public ServerResponseEntity<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateInfoRequest appAdminUpdateRequest) {
        ThrowUtil.throwIf(appAdminUpdateRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        String appId = appAdminUpdateRequest.getAppId();
        App oldApp = appService.getById(appId);
        ThrowUtil.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "app not found");

        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        app.setUpdateTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtil.throwIf(!result, ErrorCode.OPERATION_ERROR, "failed to update app");
        return ServerResponseEntity.success(true);
    }

    @PostMapping("/admin/list/page/info")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "admin list app page", description = "admin list app page")
    @Parameter(name = "appQueryRequest", description = "app query request")
    public ServerResponseEntity<Page<AppVO>> listAppInfoByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtil.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        normalizePageRequest(appQueryRequest, MAX_ADMIN_PAGE_SIZE);
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(current, pageSize), queryWrapper);

        Page<AppVO> appInfoPage = new Page<>(current, pageSize, appPage.getTotalRow());
        appInfoPage.setRecords(appService.getPrivateAppInfoList(appPage.getRecords()));
        return ServerResponseEntity.success(appInfoPage);
    }

    @GetMapping("/admin/get/info/{appId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "admin get app info", description = "admin get app info")
    @Parameter(name = "appId", description = "app id")
    public ServerResponseEntity<AppVO> getAppInfoByIdByAdmin(@PathVariable String appId) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "appId is blank");
        return ServerResponseEntity.success(appService.getPrivateAppInfo(appId));
    }

    @GetMapping("/download/{appId}")
    @Operation(summary = "download app code", description = "download app code")
    @Parameter(name = "appId", description = "app id")
    public ServerResponseEntity<Boolean> downloadAppCode(@PathVariable String appId, HttpServletResponse response) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "appId is blank");
        appService.validateAppAccess(appId);
        return ServerResponseEntity.success(projectDownloadService.downloadProjectAsZip(appId, response));
    }

    @GetMapping("/version/list/{appId}")
    @Operation(summary = "list app versions", description = "list app versions")
    @Parameter(name = "appId", description = "app id")
    public ServerResponseEntity<List<AppVersionVO>> listAppVersions(@PathVariable String appId) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "appId is blank");
        appService.validateAppAccess(appId);
        return ServerResponseEntity.success(appVersionService.listAppVersions(appId));
    }

    @PostMapping("/version/restore")
    @Operation(summary = "restore app version", description = "restore app version")
    @Parameter(name = "appVersionRestoreRequest", description = "app version restore request")
    public ServerResponseEntity<Boolean> restoreAppVersion(@RequestBody AppVersionRestoreRequest appVersionRestoreRequest) {
        ThrowUtil.throwIf(appVersionRestoreRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        ThrowUtil.throwIf(StrUtil.isBlank(appVersionRestoreRequest.getAppId()), ErrorCode.PARAMS_ERROR, "appId is blank");
        ThrowUtil.throwIf(StrUtil.isBlank(appVersionRestoreRequest.getAppVersionId()), ErrorCode.PARAMS_ERROR, "appVersionId is blank");
        appService.validateAppAccess(appVersionRestoreRequest.getAppId());
        return ServerResponseEntity.success(
                appVersionService.restoreVersion(appVersionRestoreRequest.getAppId(), appVersionRestoreRequest.getAppVersionId())
        );
    }

    private Long castToLong(Object value) {
        if (value instanceof Integer integerValue) {
            return integerValue.longValue();
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }
        return null;
    }

    private void normalizePageRequest(AppQueryRequest appQueryRequest, long maxPageSize) {
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        if (current <= 0) {
            appQueryRequest.setCurrent(1);
        }
        if (pageSize <= 0) {
            appQueryRequest.setPageSize((int) DEFAULT_PAGE_SIZE);
            return;
        }
        ThrowUtil.throwIf(pageSize > maxPageSize, ErrorCode.PARAMS_ERROR, "pageSize exceeds limit");
    }
}
