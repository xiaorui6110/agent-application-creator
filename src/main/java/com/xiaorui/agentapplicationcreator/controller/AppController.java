package com.xiaorui.agentapplicationcreator.controller;

import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppCreateRequest;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppDeployRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.AppService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用表 控制层。
 *
 * @author xiaorui
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    /**
     * 应用创建（用户在主页输入提示词）
     */
    @PostMapping("/create")
    public ServerResponseEntity<String> appCreate(@RequestBody AppCreateRequest appCreateRequest) {
        ThrowUtil.throwIf(appCreateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String appInitPrompt = appCreateRequest.getAppInitPrompt();
        String appId = appService.appCreate(appInitPrompt);
        return ServerResponseEntity.success(appId);
    }

    /**
     * 应用部署
     */
    @PostMapping("/deploy")
    public ServerResponseEntity<String> appDeploy(@RequestBody AppDeployRequest appDeployRequest) {
        ThrowUtil.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String appId = appDeployRequest.getAppId();
        String deployUrl = appService.appDeploy(appId);
        return ServerResponseEntity.success(deployUrl);
    }

    /**
     * 保存应用表。
     *
     * @param app 应用表
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody App app) {
        return appService.save(app);
    }

    /**
     * 根据主键删除应用表。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable String id) {
        return appService.removeById(id);
    }

    /**
     * 根据主键更新应用表。
     *
     * @param app 应用表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody App app) {
        return appService.updateById(app);
    }

    /**
     * 查询所有应用表。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<App> list() {
        return appService.list();
    }

    /**
     * 根据主键获取应用表。
     *
     * @param id 应用表主键
     * @return 应用表详情
     */
    @GetMapping("getInfo/{id}")
    public App getInfo(@PathVariable String id) {
        return appService.getById(id);
    }

    /**
     * 分页查询应用表。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<App> page(Page<App> page) {
        return appService.page(page);
    }

}
