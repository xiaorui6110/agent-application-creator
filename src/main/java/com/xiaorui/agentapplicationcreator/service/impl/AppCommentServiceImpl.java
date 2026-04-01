package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.AppCommentMapper;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentAddRequest;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentDeleteRequest;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentLikeRequest;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.AppComment;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.AppCommentUserVO;
import com.xiaorui.agentapplicationcreator.model.vo.AppCommentVO;
import com.xiaorui.agentapplicationcreator.model.vo.AppVO;
import com.xiaorui.agentapplicationcreator.service.AppCommentService;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentLikeRequest.LikeType.CANCEL_LIKE;
import static com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentLikeRequest.LikeType.LIKE;

/**
 * @author xiaorui
 */
@Service
public class AppCommentServiceImpl extends ServiceImpl<AppCommentMapper, AppComment> implements AppCommentService {

    private static final String ROOT_PARENT_ID = "0";

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAppComment(AppCommentAddRequest appCommentAddRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "user not found");
        }
        App app = appService.getById(appCommentAddRequest.getAppId());
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "app not found");
        }
        if (StrUtil.isBlank(appCommentAddRequest.getCommentContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "comment content is empty");
        }

        AppComment appComment = new AppComment();
        appComment.setUserId(userId);
        appComment.setAppId(appCommentAddRequest.getAppId());
        appComment.setAppUserId(app.getUserId());
        appComment.setCommentContent(appCommentAddRequest.getCommentContent());
        appComment.setParentId(StrUtil.blankToDefault(appCommentAddRequest.getParentId(), ROOT_PARENT_ID));
        appComment.setLikeCount(0L);
        appComment.setDislikeCount(0L);
        appComment.setIsDeleted(0);
        appComment.setIsRead(0);
        appComment.setCreateTime(LocalDateTime.now());
        appComment.setUpdateTime(LocalDateTime.now());
        boolean result = save(appComment);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "add app comment failed");
        }
        updateCommentCount(appCommentAddRequest.getAppId(), 1);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAppComment(AppCommentDeleteRequest appCommentDeleteRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "user not found");
        }
        AppComment appComment = getById(appCommentDeleteRequest.getCommentId());
        if (appComment == null || Integer.valueOf(1).equals(appComment.getIsDeleted())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "app comment not found");
        }
        if (!userId.equals(appComment.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "comment not belong to user");
        }

        int totalCount = countCommentsRecursively(appComment.getCommentId());
        deleteCommentsRecursively(appComment.getCommentId());
        updateCommentCount(appComment.getAppId(), -totalCount);
        return true;
    }

    @Override
    public Page<AppCommentVO> queryAppComment(AppCommentQueryRequest appCommentQueryRequest) {
        ThrowUtil.throwIf(StrUtil.isBlank(appCommentQueryRequest.getAppId()), ErrorCode.PARAMS_ERROR, "app id is empty");

        Page<AppComment> page = new Page<>(appCommentQueryRequest.getCurrent(), appCommentQueryRequest.getPageSize());
        QueryWrapper queryWrapper = new QueryWrapper()
                .eq("app_id", appCommentQueryRequest.getAppId())
                .eq("parent_id", ROOT_PARENT_ID)
                .eq("is_deleted", 0)
                .orderBy("create_time", false);
        Page<AppComment> appCommentPage = page(page, queryWrapper);

        Page<AppCommentVO> appCommentVOPage =
                new Page<>(appCommentPage.getPageNumber(), appCommentPage.getPageSize(), appCommentPage.getTotalRow());
        if (CollUtil.isEmpty(appCommentPage.getRecords())) {
            appCommentVOPage.setRecords(Collections.emptyList());
            return appCommentVOPage;
        }

        appCommentVOPage.setRecords(buildCommentVOList(appCommentPage.getRecords(), true));
        return appCommentVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean likeAppComment(AppCommentLikeRequest appCommentLikeRequest) {
        ThrowUtil.throwIf(appCommentLikeRequest == null, ErrorCode.PARAMS_ERROR, "comment like request is null");
        ThrowUtil.throwIf(StrUtil.isBlank(appCommentLikeRequest.getCommentId()), ErrorCode.PARAMS_ERROR, "comment id is empty");
        ThrowUtil.throwIf(appCommentLikeRequest.getLikeType() == null, ErrorCode.PARAMS_ERROR, "like type is empty");

        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_LOGIN_ERROR, "user not logged in");
        User loginUser = userService.getById(userId);
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "user not found");

        AppComment comment = this.getById(appCommentLikeRequest.getCommentId());
        ThrowUtil.throwIf(comment == null || Integer.valueOf(1).equals(comment.getIsDeleted()),
                ErrorCode.NOT_FOUND_ERROR, "comment not found");

        long currentLikeCount = comment.getLikeCount() == null ? 0L : comment.getLikeCount();
        if (appCommentLikeRequest.getLikeType() == LIKE) {
            comment.setLikeCount(currentLikeCount + 1);
        } else if (appCommentLikeRequest.getLikeType() == CANCEL_LIKE) {
            comment.setLikeCount(Math.max(0L, currentLikeCount - 1));
        }
        comment.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(comment);
        ThrowUtil.throwIf(!updated, ErrorCode.OPERATION_ERROR, "failed to update comment like count");
        return true;
    }

    @Override
    public List<AppCommentVO> listUnreadAppComments(String userId, int limit) {
        int finalLimit = Math.max(1, limit);
        QueryWrapper queryWrapper = new QueryWrapper()
                .eq("app_user_id", userId)
                .eq("is_read", 0)
                .eq("is_deleted", 0)
                .ne("user_id", userId)
                .orderBy("create_time", false)
                .limit(finalLimit);
        List<AppComment> unreadComments = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadComments)) {
            return new ArrayList<>();
        }
        return buildCommentVOList(unreadComments, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markCommentsAsRead(String userId, List<String> commentIds) {
        if (StrUtil.isBlank(userId) || CollUtil.isEmpty(commentIds)) {
            return;
        }
        UpdateChain.of(AppComment.class)
                .setRaw(AppComment::getIsRead, 1)
                .where(AppComment::getAppUserId).eq(userId)
                .and(AppComment::getCommentId).in(commentIds)
                .and(AppComment::getIsRead).eq(0)
                .and(AppComment::getIsDeleted).eq(0)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AppCommentVO> getAndClearUnreadAppComment(String userId) {
        List<AppCommentVO> unreadComments = listUnreadAppComments(userId, 50);
        if (CollUtil.isEmpty(unreadComments)) {
            return new ArrayList<>();
        }
        markCommentsAsRead(userId, unreadComments.stream().map(AppCommentVO::getCommentId).toList());
        return unreadComments;
    }

    @Override
    public long getUnreadCommentsCount(String userId) {
        return this.count(new QueryWrapper()
                .eq("app_user_id", userId)
                .eq("is_read", 0)
                .eq("is_deleted", 0)
                .ne("user_id", userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllUnreadComments(String userId) {
        UpdateChain.of(AppComment.class)
                .setRaw(AppComment::getIsRead, 1)
                .where(AppComment::getAppUserId).eq(userId)
                .and(AppComment::getIsRead).eq(0)
                .update();
    }

    @Override
    public Page<AppCommentVO> getCommentedHistory(AppCommentQueryRequest appCommentQueryRequest, String userId) {
        ThrowUtil.throwIf(appCommentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppComment> page = new Page<>(appCommentQueryRequest.getCurrent(), appCommentQueryRequest.getPageSize());
        QueryWrapper queryWrapper = new QueryWrapper()
                .eq("app_user_id", userId)
                .eq("is_deleted", 0)
                .ne("user_id", userId)
                .orderBy("create_time", false);
        Page<AppComment> appCommentPage = this.page(page, queryWrapper);
        Page<AppCommentVO> result =
                new Page<>(appCommentPage.getPageNumber(), appCommentPage.getPageSize(), appCommentPage.getTotalRow());
        result.setRecords(buildCommentVOList(appCommentPage.getRecords(), false));
        return result;
    }

    @Override
    public Page<AppCommentVO> getMyCommentHistory(AppCommentQueryRequest appCommentQueryRequest, String userId) {
        ThrowUtil.throwIf(appCommentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppComment> page = new Page<>(appCommentQueryRequest.getCurrent(), appCommentQueryRequest.getPageSize());
        QueryWrapper queryWrapper = new QueryWrapper()
                .eq("user_id", userId)
                .eq("is_deleted", 0)
                .orderBy("create_time", false);
        Page<AppComment> appCommentPage = this.page(page, queryWrapper);
        Page<AppCommentVO> result =
                new Page<>(appCommentPage.getPageNumber(), appCommentPage.getPageSize(), appCommentPage.getTotalRow());
        result.setRecords(buildCommentVOList(appCommentPage.getRecords(), false));
        return result;
    }

    private void updateCommentCount(String appId, int delta) {
        App app = appService.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "app not found");
        }
        long currentCount = app.getCommentCount() == null ? 0 : app.getCommentCount();
        app.setCommentCount(Math.max(0, currentCount + delta));
        boolean updated = appService.updateById(app);
        ThrowUtil.throwIf(!updated, ErrorCode.OPERATION_ERROR, "failed to update app comment count");
    }

    private int countCommentsRecursively(String commentId) {
        List<AppComment> childComments = list(new QueryWrapper()
                .eq("parent_id", commentId)
                .eq("is_deleted", 0));
        int totalCount = 1;
        if (CollUtil.isNotEmpty(childComments)) {
            for (AppComment childComment : childComments) {
                totalCount += countCommentsRecursively(childComment.getCommentId());
            }
        }
        return totalCount;
    }

    private void deleteCommentsRecursively(String commentId) {
        List<AppComment> childComments = list(new QueryWrapper()
                .eq("parent_id", commentId)
                .eq("is_deleted", 0));
        if (CollUtil.isNotEmpty(childComments)) {
            for (AppComment childComment : childComments) {
                deleteCommentsRecursively(childComment.getCommentId());
            }
        }
        AppComment currentComment = getById(commentId);
        if (currentComment != null && !Integer.valueOf(1).equals(currentComment.getIsDeleted())) {
            currentComment.setIsDeleted(1);
            currentComment.setUpdateTime(LocalDateTime.now());
            updateById(currentComment);
        }
    }

    private List<AppCommentVO> getChildrenComments(String parentCommentId) {
        List<AppComment> childrenComments = this.list(new QueryWrapper()
                .eq("parent_id", parentCommentId)
                .eq("is_deleted", 0)
                .orderBy("create_time", false));
        return buildCommentVOList(childrenComments, true);
    }

    private List<AppCommentVO> buildCommentVOList(List<AppComment> comments, boolean includeChildren) {
        if (CollUtil.isEmpty(comments)) {
            return Collections.emptyList();
        }
        Map<String, AppCommentUserVO> userMap = userService.listByIds(comments.stream()
                        .map(AppComment::getUserId)
                        .distinct()
                        .toList())
                .stream()
                .map(user -> BeanUtil.copyProperties(user, AppCommentUserVO.class))
                .collect(Collectors.toMap(AppCommentUserVO::getUserId, item -> item));
        Map<String, AppVO> appMap = appService.listByIds(comments.stream()
                        .map(AppComment::getAppId)
                        .distinct()
                        .toList())
                .stream()
                .map(app -> BeanUtil.copyProperties(app, AppVO.class))
                .collect(Collectors.toMap(AppVO::getAppId, item -> item));

        return comments.stream().map(comment -> {
            AppCommentVO appCommentVO = BeanUtil.copyProperties(comment, AppCommentVO.class);
            appCommentVO.setAppCommentUserVO(userMap.get(comment.getUserId()));
            appCommentVO.setAppVO(appMap.get(comment.getAppId()));
            if (includeChildren) {
                appCommentVO.setChildCommentList(getChildrenComments(comment.getCommentId()));
            }
            return appCommentVO;
        }).collect(Collectors.toList());
    }
}
