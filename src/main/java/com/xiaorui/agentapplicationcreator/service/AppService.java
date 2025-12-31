package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.vo.AppVO;

import java.util.List;

/**
 * 应用表 服务层。
 *
 * @author xiaorui
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     *
     * @param appInitPrompt 应用初始化prompt
     * @return 应用id
     */
    String appCreate(String appInitPrompt);

    /**
     * 获取查询条件（通过appId、appName和codeGenType查询）
     *
     * @param appQueryRequest 应用查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 部署应用
     *
     * @param appId 应用id
     * @return 部署结果url
     */
    String appDeploy(String appId);


    /**
     * 获取应用信息
     *
     * @param appId 应用id
     * @return 应用信息vo
     */
    AppVO getAppInfo(String appId);

    /**
     * 获取应用信息列表
     *
     * @param appList 应用列表
     * @return 应用信息列表
     */
    List<AppVO> getAppInfoList(List<App> appList);

}
