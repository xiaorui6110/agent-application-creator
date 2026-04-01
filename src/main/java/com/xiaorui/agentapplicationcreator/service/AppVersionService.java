package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.entity.AppVersion;
import com.xiaorui.agentapplicationcreator.model.vo.AppVersionVO;

import java.util.List;

/**
 * @author xiaorui
 */
public interface AppVersionService extends IService<AppVersion> {

    AppVersion createVersionSnapshot(String appId, String versionSource, String versionNote, String deployUrl);

    List<AppVersionVO> listAppVersions(String appId);

    boolean restoreVersion(String appId, String appVersionId);
}
