package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.entity.AppVersion;
import com.xiaorui.agentapplicationcreator.model.vo.AppVersionVO;

import java.util.List;

/**
 * @author xiaorui
 */
public interface AppVersionService extends IService<AppVersion> {

    /**
     * 创建版本快照
     * @param appId 应用ID
     * @param versionSource 版本来源
     * @param versionNote 版本备注
     * @param deployUrl 部署URL
     * @return 版本快照
     */
    AppVersion createVersionSnapshot(String appId, String versionSource, String versionNote, String deployUrl);

    /**
     * 列出应用版本
     * @param appId 应用ID
     * @return 应用版本列表
     */
    List<AppVersionVO> listAppVersions(String appId);

    /**
     * 恢复版本
     * @param appId 应用ID
     * @param appVersionId 应用版本ID
     * @return 恢复版本结果
     */
    boolean restoreVersion(String appId, String appVersionId);
}
