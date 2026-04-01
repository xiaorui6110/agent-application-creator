package com.xiaorui.agentapplicationcreator.service;

import com.xiaorui.agentapplicationcreator.model.vo.CommunityNotificationVO;
import com.xiaorui.agentapplicationcreator.model.vo.CommunityUnreadSummaryVO;

import java.util.List;

/**
 * @author xiaorui
 */
public interface CommunityService {

    CommunityUnreadSummaryVO getUnreadSummary(String userId);

    List<CommunityNotificationVO> getAndClearUnreadNotifications(String userId, Integer limit);

    void clearAllUnreadNotifications(String userId);
}
