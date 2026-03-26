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

@Service
public class AppCommentServiceImpl extends ServiceImpl<AppCommentMapper, AppComment> implements AppCommentService {

    private static final String ROOT_PARENT_ID = "0";

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    @Override
    public Boolean addAppComment(AppCommentAddRequest appCommentAddRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "鐢ㄦ埛涓嶅瓨鍦?");
        }
        App app = appService.getById(appCommentAddRequest.getAppId());
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "搴旂敤涓嶅瓨鍦?");
        }
        if (StrUtil.isBlank(appCommentAddRequest.getCommentContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "璇勮涓嶈兘涓虹┖");
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
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "璇勮淇濆瓨澶辫触");
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
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "鐢ㄦ埛涓嶅瓨鍦?");
        }
        AppComment appComment = getById(appCommentDeleteRequest.getCommentId());
        if (appComment == null || Integer.valueOf(1).equals(appComment.getIsDeleted())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "璇勮涓嶅瓨鍦?");
        }
        if (!userId.equals(appComment.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "鏃犳潈鍒犻櫎浠栦汉璇勮");
        }

        int totalCount = countCommentsRecursively(appComment.getCommentId());
        deleteCommentsRecursively(appComment.getCommentId());
        updateCommentCount(appComment.getAppId(), -totalCount);
        return true;
    }

    @Override
    public Page<AppCommentVO> queryAppComment(AppCommentQueryRequest appCommentQueryRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "鐢ㄦ埛涓嶅瓨鍦?");
        }
        ThrowUtil.throwIf(StrUtil.isBlank(appCommentQueryRequest.getAppId()), ErrorCode.PARAMS_ERROR, "搴旂敤ID涓嶈兘涓虹┖");

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
    public Boolean likeAppComment(AppCommentLikeRequest appCommentLikeRequest) {
        ThrowUtil.throwIf(appCommentLikeRequest.getCommentId() == null, ErrorCode.PARAMS_ERROR, "璇勮ID涓嶈兘涓虹┖");
        ThrowUtil.throwIf(appCommentLikeRequest.getLikeType() == null, ErrorCode.PARAMS_ERROR, "鐐硅禐绫诲瀷涓嶈兘涓虹┖");

        if (appCommentLikeRequest.getLikeType() == LIKE || appCommentLikeRequest.getLikeCount() != 0) {
            UpdateChain.of(AppComment.class)
                    .setRaw(AppComment::getLikeCount, "like_count + 1")
                    .where(AppComment::getCommentId).eq(appCommentLikeRequest.getCommentId())
                    .update();
        }
        if (appCommentLikeRequest.getLikeType() == CANCEL_LIKE) {
            UpdateChain.of(AppComment.class)
                    .setRaw(AppComment::getLikeCount, "like_count - 1")
                    .where(AppComment::getCommentId).eq(appCommentLikeRequest.getCommentId())
                    .update();
        }
        if (appCommentLikeRequest.getDislikeCount() != null && appCommentLikeRequest.getDislikeCount() != 0) {
            UpdateChain.of(AppComment.class)
                    .setRaw(AppComment::getDislikeCount, "dislike_count + 1")
                    .where(AppComment::getCommentId).eq(appCommentLikeRequest.getCommentId())
                    .update();
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AppCommentVO> getAndClearUnreadAppComment(String userId) {
        QueryWrapper queryWrapper = new QueryWrapper()
                .eq("app_user_id", userId)
                .eq("is_read", 0)
                .eq("is_deleted", 0)
                .ne("user_id", userId)
                .orderBy("create_time", false)
                .limit(50);
        List<AppComment> unreadComments = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadComments)) {
            return new ArrayList<>();
        }

        List<String> commentIds = unreadComments.stream().map(AppComment::getCommentId).toList();
        UpdateChain.of(AppComment.class)
                .setRaw(AppComment::getIsRead, 1)
                .where(AppComment::getCommentId).in(commentIds)
                .update();
        return buildCommentVOList(unreadComments, true);
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
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "搴旂敤涓嶅瓨鍦?");
        }
        long currentCount = app.getCommentCount() == null ? 0 : app.getCommentCount();
        app.setCommentCount(Math.max(0, currentCount + delta));
        appService.updateById(app);
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
