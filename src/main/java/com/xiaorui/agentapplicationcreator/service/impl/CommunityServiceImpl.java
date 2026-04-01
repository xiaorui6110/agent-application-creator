package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaorui.agentapplicationcreator.model.vo.AppCommentVO;
import com.xiaorui.agentapplicationcreator.model.vo.CommunityNotificationVO;
import com.xiaorui.agentapplicationcreator.model.vo.CommunityUnreadSummaryVO;
import com.xiaorui.agentapplicationcreator.model.vo.LikeRecordVO;
import com.xiaorui.agentapplicationcreator.model.vo.ShareRecordVO;
import com.xiaorui.agentapplicationcreator.service.AppCommentService;
import com.xiaorui.agentapplicationcreator.service.CommunityService;
import com.xiaorui.agentapplicationcreator.service.LikeRecordService;
import com.xiaorui.agentapplicationcreator.service.ShareRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xiaorui
 */
@Service
public class CommunityServiceImpl implements CommunityService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    @Resource
    private AppCommentService appCommentService;

    @Resource
    private LikeRecordService likeRecordService;

    @Resource
    private ShareRecordService shareRecordService;

    @Override
    public CommunityUnreadSummaryVO getUnreadSummary(String userId) {
        long unreadCommentCount = appCommentService.getUnreadCommentsCount(userId);
        long unreadLikeCount = likeRecordService.getUnreadLikesCount(userId);
        long unreadShareCount = shareRecordService.getUnreadSharesCount(userId);

        CommunityUnreadSummaryVO summaryVO = new CommunityUnreadSummaryVO();
        summaryVO.setUnreadCommentCount(unreadCommentCount);
        summaryVO.setUnreadLikeCount(unreadLikeCount);
        summaryVO.setUnreadShareCount(unreadShareCount);
        summaryVO.setTotalUnreadCount(unreadCommentCount + unreadLikeCount + unreadShareCount);
        return summaryVO;
    }

    @Override
    public List<CommunityNotificationVO> getAndClearUnreadNotifications(String userId, Integer limit) {
        int finalLimit = normalizeLimit(limit);
        List<CommunityNotificationVO> notifications = new ArrayList<>();
        notifications.addAll(appCommentService.listUnreadAppComments(userId, finalLimit).stream().map(this::fromComment).toList());
        notifications.addAll(likeRecordService.listUnreadLikes(userId, finalLimit).stream().map(this::fromLike).toList());
        notifications.addAll(shareRecordService.listUnreadShares(userId, finalLimit).stream().map(this::fromShare).toList());

        List<CommunityNotificationVO> result = notifications.stream()
                .sorted(Comparator.comparing(CommunityNotificationVO::getActionTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(finalLimit)
                .toList();
        markReturnedNotificationsAsRead(userId, result);
        return result;
    }

    @Override
    public void clearAllUnreadNotifications(String userId) {
        appCommentService.clearAllUnreadComments(userId);
        likeRecordService.clearAllUnreadLikes(userId);
        shareRecordService.clearAllUnreadShares(userId);
    }

    private CommunityNotificationVO fromComment(AppCommentVO commentVO) {
        CommunityNotificationVO notificationVO = new CommunityNotificationVO();
        notificationVO.setNotificationType("COMMENT");
        notificationVO.setNotificationId(commentVO.getCommentId());
        notificationVO.setActorUserId(commentVO.getUserId());
        notificationVO.setActorUserName(commentVO.getAppCommentUserVO() == null ? null : commentVO.getAppCommentUserVO().getNickName());
        notificationVO.setAppId(commentVO.getAppId());
        notificationVO.setAppName(commentVO.getAppVO() == null ? null : commentVO.getAppVO().getAppName());
        notificationVO.setContent(commentVO.getCommentContent());
        notificationVO.setActionTime(commentVO.getCreateTime());
        return notificationVO;
    }

    private CommunityNotificationVO fromLike(LikeRecordVO likeRecordVO) {
        CommunityNotificationVO notificationVO = new CommunityNotificationVO();
        notificationVO.setNotificationType("LIKE");
        notificationVO.setNotificationId(likeRecordVO.getLikeId());
        notificationVO.setActorUserId(likeRecordVO.getUserId());
        notificationVO.setActorUserName(likeRecordVO.getUserVO() == null ? null : likeRecordVO.getUserVO().getNickName());
        notificationVO.setAppId(likeRecordVO.getTargetId());
        notificationVO.setAppName(likeRecordVO.getAppVO() == null ? null : likeRecordVO.getAppVO().getAppName());
        notificationVO.setContent(buildActionContent(likeRecordVO.getAppVO() == null ? null : likeRecordVO.getAppVO().getAppName(), "liked your app"));
        notificationVO.setActionTime(likeRecordVO.getLastLikeTime());
        return notificationVO;
    }

    private CommunityNotificationVO fromShare(ShareRecordVO shareRecordVO) {
        CommunityNotificationVO notificationVO = new CommunityNotificationVO();
        notificationVO.setNotificationType("SHARE");
        notificationVO.setNotificationId(shareRecordVO.getShareId());
        notificationVO.setActorUserId(shareRecordVO.getUserId());
        notificationVO.setActorUserName(shareRecordVO.getUserVO() == null ? null : shareRecordVO.getUserVO().getNickName());
        notificationVO.setAppId(shareRecordVO.getTargetId());
        notificationVO.setAppName(shareRecordVO.getAppVO() == null ? null : shareRecordVO.getAppVO().getAppName());
        notificationVO.setContent(buildActionContent(shareRecordVO.getAppVO() == null ? null : shareRecordVO.getAppVO().getAppName(), "shared your app"));
        notificationVO.setActionTime(shareRecordVO.getShareTime());
        return notificationVO;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String buildActionContent(String appName, String suffix) {
        if (StrUtil.isBlank(appName)) {
            return suffix;
        }
        return appName + " - " + suffix;
    }

    private void markReturnedNotificationsAsRead(String userId, List<CommunityNotificationVO> notifications) {
        if (CollUtil.isEmpty(notifications)) {
            return;
        }
        Map<String, List<String>> idsByType = notifications.stream()
                .filter(notification -> StrUtil.isNotBlank(notification.getNotificationType())
                        && StrUtil.isNotBlank(notification.getNotificationId()))
                .collect(Collectors.groupingBy(CommunityNotificationVO::getNotificationType,
                        Collectors.mapping(CommunityNotificationVO::getNotificationId, Collectors.toList())));
        appCommentService.markCommentsAsRead(userId, idsByType.get("COMMENT"));
        likeRecordService.markLikesAsRead(userId, idsByType.get("LIKE"));
        shareRecordService.markSharesAsRead(userId, idsByType.get("SHARE"));
    }
}
