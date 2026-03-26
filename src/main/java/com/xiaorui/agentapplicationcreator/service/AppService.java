package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.vo.AppVO;

import java.util.List;

public interface AppService extends IService<App> {

    String createApp(String appInitPrompt);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    String deployApp(String appId);

    AppVO getAppInfo(String appId);

    List<AppVO> getAppInfoList(List<App> appList);

    List<AppVO> getMyAppInfoList(List<App> appList);

    List<AppVO> getAppInfoListForGoods(List<App> appList);

    void createAppScreenshotAsync(String appId, String appDeploy);

    void updateAppNameAsync(String appId, String appName);

    void updateAppCodeGenTypeAsync(String appId, String codeGenType);

    Page<AppVO> listRecommendedApps(AppQueryRequest appQueryRequest);

    Page<AppVO> listRankedApps(AppQueryRequest appQueryRequest);

    List<String> listAppCategories();
}
