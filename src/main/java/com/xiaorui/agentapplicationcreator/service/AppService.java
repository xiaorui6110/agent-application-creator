package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.vo.AppVO;

import java.util.List;

/**
 * @author xiaorui
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     * @param appInitPrompt 应用初始化提示
     * @return 应用ID
     */
    String createApp(String appInitPrompt);

    /**
     * 获取查询条件
     * @param appQueryRequest 应用查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 部署应用
     * @param appId 应用ID
     * @return 部署结果
     */
    String deployApp(String appId);

    /**
     * 获取应用信息
     * @param appId 应用ID
     * @return 应用信息
     */
    AppVO getAppInfo(String appId);

    /**
     * 获取应用信息列表
     * @param appList 应用列表
     * @return 应用信息列表
     */
    List<AppVO> getAppInfoList(List<App> appList);

    /**
     * 获取我的应用信息列表
     * @param appList 应用列表
     * @return 我的应用信息列表
     */
    List<AppVO> getMyAppInfoList(List<App> appList);

    /**
     * 获取应用信息列表
     * @param appList 应用列表
     * @return 应用信息列表
     */
    List<AppVO> getAppInfoListForGoods(List<App> appList);

    /**
     * 创建应用截图
     * @param appId 应用ID
     * @param appDeploy 应用部署
     */
    void createAppScreenshotAsync(String appId, String appDeploy);

    /**
     * 更新应用名称
     * @param appId 应用ID
     * @param appName 应用名称
     */
    void updateAppNameAsync(String appId, String appName);

    /**
     * 更新应用代码生成类型
     * @param appId 应用ID
     * @param codeGenType 代码生成类型
     */
    void updateAppCodeGenTypeAsync(String appId, String codeGenType);

    /**
     * 获取推荐应用列表
     * @param appQueryRequest 应用查询请求
     * @return 推荐应用列表
     */
    Page<AppVO> listRecommendedApps(AppQueryRequest appQueryRequest);

    /**
     * 获取排行榜应用列表
     * @param appQueryRequest 应用查询请求
     * @return 排行榜应用列表
     */
    Page<AppVO> listRankedApps(AppQueryRequest appQueryRequest);

    /**
     * 获取应用分类列表
     * @return 应用分类列表
     */
    List<String> listAppCategories();
}
