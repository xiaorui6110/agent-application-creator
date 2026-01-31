package com.xiaorui.agentapplicationcreator.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xiaorui.agentapplicationcreator.common.DeleteRequest;
import com.xiaorui.agentapplicationcreator.constant.AppConstant;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import com.xiaorui.agentapplicationcreator.model.dto.app.*;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.AppVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.ProjectDownloadService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.RedisCacheUtil;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.xiaorui.agentapplicationcreator.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

/**
 * 应用表 控制层。
 *
 * @author xiaorui
 */
//@Tag(name = "应用接口")
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private ProjectDownloadService projectDownloadService;

    @Resource
    private RedisCacheUtil redisCacheUtil;

    /**
     * 应用创建（用户在主页输入提示词）
     */
    @PostMapping("/create")
    @Operation(summary = "应用创建" , description = "用户在主页输入提示词")
    @Parameter(name = "appCreateRequest", description = "应用创建请求")
    public ServerResponseEntity<String> createApp(@RequestBody AppCreateRequest appCreateRequest) {
        ThrowUtil.throwIf(appCreateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String appInitPrompt = appCreateRequest.getAppInitPrompt();
        String appId = appService.createApp(appInitPrompt);
        return ServerResponseEntity.success(appId);
    }

    /**
     * 应用部署
     */
    @PostMapping("/deploy")
    @Operation(summary = "应用部署" , description = "应用部署")
    @Parameter(name = "appDeployRequest", description = "应用部署请求")
    public ServerResponseEntity<String> deployApp(@RequestBody AppDeployRequest appDeployRequest) {
        ThrowUtil.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String appId = appDeployRequest.getAppId();
        String deployUrl = appService.deployApp(appId);
        return ServerResponseEntity.success(deployUrl);
    }

    /**
     * 应用信息更新（目前只是更新应用的名称、封面和描述）
     */
    @PostMapping("/update")
    @Operation(summary = "应用信息更新" , description = "应用信息更新")
    @Parameter(name = "appUpdateRequest", description = "应用信息更新请求")
    public ServerResponseEntity<Boolean> updateApp(@RequestBody AppUpdateInfoRequest appUpdateRequest) {
        ThrowUtil.throwIf(appUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String appId = appUpdateRequest.getAppId();
        // 判断是否存在
        App oldApp = appService.getById(appId);
        ThrowUtil.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        // 仅本人可更新
        ThrowUtil.throwIf(!oldApp.getUserId().equals(SecurityUtil.getUserInfo().getUserId()), ErrorCode.NOT_AUTH_ERROR,"无权限操作");
        App app = new App();
        app.setAppId(appId);
        app.setAppName(appUpdateRequest.getAppName());
        app.setAppCover(appUpdateRequest.getAppCover());
        app.setAppDescription(appUpdateRequest.getAppDescription());
        // 设置编辑时间
        app.setUpdateTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtil.throwIf(!result, ErrorCode.OPERATION_ERROR,"应用更新失败");
        return ServerResponseEntity.success(true);
    }

    /**
     * 获取应用信息（包含用户信息）
     */
    @GetMapping("/get/info/{appId}")
    @Operation(summary = "获取应用信息" , description = "获取应用信息")
    @Parameter(name = "appId", description = "应用id")
    public ServerResponseEntity<AppVO> getAppInfoById(@PathVariable String appId) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR,"应用id不能为空");
        // 查询数据库
        App app = appService.getById(appId);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 获取封装类（包含用户信息）
        return ServerResponseEntity.success(appService.getAppInfo(appId));
    }

    /**
     * 删除应用
     */
    @PostMapping("/delete")
    @Operation(summary = "删除应用" , description = "删除应用")
    @Parameter(name = "deleteRequest", description = "删除请求")
    public ServerResponseEntity<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtil.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        User loginUser = userService.getById(SecurityUtil.getUserInfo().getUserId());
        String deleteRequestId = deleteRequest.getId();
        ThrowUtil.throwIf(StrUtil.isBlank(deleteRequestId), ErrorCode.PARAMS_ERROR, "应用id不能为空");
        // 判断是否存在
        App oldApp = appService.getById(deleteRequestId);
        ThrowUtil.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 仅本人或管理员可删除
        if (!oldApp.getUserId().equals(SecurityUtil.getUserInfo().getUserId())
                && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权限操作");
        }
        return ServerResponseEntity.success(appService.removeById(deleteRequestId));
    }

    /**
     * 分页获取应用列表
     */
    @PostMapping("/list/page/info")
    @Operation(summary = "分页获取应用列表" , description = "分页获取应用列表")
    @Parameter(name = "appQueryRequest", description = "应用查询请求")
    public ServerResponseEntity<Page<AppVO>> listAppInfoByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtil.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(current, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appInfoPage = new Page<>(current, pageSize, appPage.getTotalRow());
        List<AppVO> appInfoList = appService.getAppInfoList(appPage.getRecords());
        appInfoPage.setRecords(appInfoList);
        return ServerResponseEntity.success(appInfoPage);
    }

    /**
     * 分页获取精选应用列表（使用手动缓存，避免 Page 反序列化问题）
     *
     * @param appQueryRequest 查询请求
     * @return 精选应用列表
     */
    @PostMapping("/good/list/page/info")
    @Operation(summary = "分页获取精选应用列表" , description = "分页获取精选应用列表")
    @Parameter(name = "appQueryRequest", description = "应用查询请求")
    public ServerResponseEntity<Page<AppVO>> listGoodAppInfoByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtil.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtil.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        
        // 构造缓存键
        String cacheKey = "good_app:list:" + current + ":" + pageSize;
        
        // 尝试从缓存获取数据
        List<AppVO> cachedList = redisCacheUtil.get(cacheKey + ":data");
        Object cachedTotalObj = redisCacheUtil.get(cacheKey + ":total");
        
        // 处理缓存的 total（可能是 Integer 或 Long）
        Long cachedTotal = null;
        if (cachedTotalObj != null) {
            if (cachedTotalObj instanceof Integer) {
                cachedTotal = ((Integer) cachedTotalObj).longValue();
            } else if (cachedTotalObj instanceof Long) {
                cachedTotal = (Long) cachedTotalObj;
            } else if (cachedTotalObj instanceof Number) {
                cachedTotal = ((Number) cachedTotalObj).longValue();
            }
        }
        
        if (cachedList != null && cachedTotal != null) {
            // 缓存命中，构造 Page 对象
            Page<AppVO> appInfoPage = new Page<>(current, pageSize, cachedTotal);
            appInfoPage.setRecords(cachedList);
            return ServerResponseEntity.success(appInfoPage);
        }
        
        // 缓存未命中，查询数据库
        appQueryRequest.setAppPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(current, pageSize), queryWrapper);
        
        // 数据封装
        Page<AppVO> appInfoPage = new Page<>(current, pageSize, appPage.getTotalRow());
        List<AppVO> appInfoList = appService.getAppInfoListForGoods(appPage.getRecords());
        appInfoPage.setRecords(appInfoList);
        
        // 将数据存入缓存（缓存 5 分钟）
        redisCacheUtil.set(cacheKey + ":data", appInfoList, 5, TimeUnit.MINUTES);
        redisCacheUtil.set(cacheKey + ":total", appPage.getTotalRow(), 5, TimeUnit.MINUTES);
        
        return ServerResponseEntity.success(appInfoPage);
    }

    /**
     * 【管理员】删除应用
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员删除应用" , description = "管理员删除应用")
    @Parameter(name = "deleteRequest", description = "删除请求")
    public ServerResponseEntity<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtil.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String deleteRequestId = deleteRequest.getId();
        ThrowUtil.throwIf(StrUtil.isBlank(deleteRequestId), ErrorCode.PARAMS_ERROR, "应用id不能为空");
        // 判断是否存在
        App oldApp = appService.getById(deleteRequestId);
        ThrowUtil.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        return ServerResponseEntity.success(appService.removeById(deleteRequestId));
    }

    /**
     * 【管理员】更新应用
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员更新应用" , description = "管理员更新应用")
    @Parameter(name = "appAdminUpdateRequest", description = "管理员更新请求")
    public ServerResponseEntity<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateInfoRequest appAdminUpdateRequest) {
        ThrowUtil.throwIf(appAdminUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String appId = appAdminUpdateRequest.getAppId();
        // 判断是否存在
        App oldApp = appService.getById(appId);
        ThrowUtil.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        app.setUpdateTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtil.throwIf(!result, ErrorCode.OPERATION_ERROR,"应用更新失败");
        return ServerResponseEntity.success(true);
    }

    /**
     * 【管理员】分页获取应用列表
     */
    @PostMapping("/admin/list/page/info")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员分页获取应用列表" , description = "管理员分页获取应用列表")
    @Parameter(name = "appQueryRequest", description = "应用查询请求")
    public ServerResponseEntity<Page<AppVO>> listAppInfoByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtil.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        long current = appQueryRequest.getCurrent();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(current, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appInfoPage = new Page<>(current, pageSize, appPage.getTotalRow());
        List<AppVO> appInfoList = appService.getAppInfoList(appPage.getRecords());
        appInfoPage.setRecords(appInfoList);
        return ServerResponseEntity.success(appInfoPage);
    }

    /**
     * 【管理员】根据 id 获取应用详情
     */
    @GetMapping("/admin/get/info/{appId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员根据 id 获取应用详情" , description = "管理员根据 id 获取应用详情")
    @Parameter(name = "appId", description = "应用id")
    public ServerResponseEntity<AppVO> getAppInfoByIdByAdmin(@PathVariable String appId) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "应用id不能为空");
        return ServerResponseEntity.success(appService.getAppInfo(appId));
    }

    /**
     *  下载应用代码
     */
    @GetMapping("/download/{appId}")
    @Operation(summary = "下载应用代码" , description = "下载应用代码")
    @Parameter(name = "appId", description = "应用id")
    public ServerResponseEntity<Boolean> downloadAppCode(@PathVariable String appId, HttpServletResponse response) {
        ThrowUtil.throwIf(StrUtil.isBlank(appId), ErrorCode.PARAMS_ERROR, "应用id不能为空");
        App app = appService.getById(appId);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        User loginUser = userService.getById(SecurityUtil.getUserInfo().getUserId());
        if (!app.getUserId().equals(loginUser.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权限下载该应用代码");
        }
        String sourceDirPath = CODE_OUTPUT_ROOT_DIR + File.separator + appId;
        File sourceDir = new File(sourceDirPath);
        ThrowUtil.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");
        return ServerResponseEntity.success(projectDownloadService.downloadProjectAsZip(sourceDirPath, appId, response));
    }

}
