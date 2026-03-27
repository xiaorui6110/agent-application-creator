package com.xiaorui.agentapplicationcreator.service;

import com.xiaorui.agentapplicationcreator.model.vo.CommunityNotificationVO;
import com.xiaorui.agentapplicationcreator.model.vo.CommunityUnreadSummaryVO;

import java.util.List;

/**
 * @author xiaorui
 */
public interface CommunityService {

    /**
     * 获取未读消息摘要
     * @param userId 用户ID
     * @return 未读消息摘要
     */
    CommunityUnreadSummaryVO getUnreadSummary(String userId);

    /**
     * 获取并清除未读消息
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 未读消息列表
     */
    List<CommunityNotificationVO> getAndClearUnreadNotifications(String userId, Integer limit);

    /**
     * 清除所有未读消息
     * @param userId 用户ID
     */
    void clearAllUnreadNotifications(String userId);
}
