package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.LikeRecordMapper;
import com.xiaorui.agentapplicationcreator.model.dto.likerecord.LikeDoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.likerecord.LikeQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.LikeRecord;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.AppVO;
import com.xiaorui.agentapplicationcreator.model.vo.LikeRecordVO;
import com.xiaorui.agentapplicationcreator.model.vo.UserVO;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.LikeRecordService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaorui
 */
@Slf4j
@Service
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements LikeRecordService {

    @Lazy
    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doLike(LikeDoRequest likeDoRequest, String userId) {
        ThrowUtil.throwIf(likeDoRequest == null, ErrorCode.PARAMS_ERROR, "like request is null");
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_LOGIN_ERROR, "user not logged in");

        String targetId = likeDoRequest.getTargetId();
        int isLiked = likeDoRequest.getIsLiked();
        ThrowUtil.throwIf(StrUtil.isBlank(targetId), ErrorCode.PARAMS_ERROR, "targetId is blank");
        ThrowUtil.throwIf(isLiked != 0 && isLiked != 1, ErrorCode.PARAMS_ERROR, "isLiked must be 0 or 1");

        User loginUser = userService.getById(userId);
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "user not found");
        App targetApp = appService.getById(targetId);
        ThrowUtil.throwIf(targetApp == null, ErrorCode.NOT_FOUND_ERROR, "target app not found");

        LikeRecord oldLikeRecord = this.getOne(new QueryWrapper()
                .eq("user_id", userId)
                .eq("target_id", targetId));
        if (oldLikeRecord == null) {
            if (isLiked == 0) {
                return true;
            }
            LikeRecord likeRecord = new LikeRecord();
            likeRecord.setUserId(userId);
            likeRecord.setTargetId(targetId);
            likeRecord.setTargetUserId(targetApp.getUserId());
            likeRecord.setIsLiked(1);
            likeRecord.setFirstLikeTime(LocalDateTime.now());
            likeRecord.setLastLikeTime(LocalDateTime.now());
            likeRecord.setIsRead(0);
            boolean saved = this.save(likeRecord);
            ThrowUtil.throwIf(!saved, ErrorCode.OPERATION_ERROR, "failed to create like record");
            updateLikeCount(targetApp, 1);
            return true;
        }

        if (isLiked == oldLikeRecord.getIsLiked()) {
            return true;
        }
        oldLikeRecord.setIsLiked(isLiked);
        oldLikeRecord.setLastLikeTime(LocalDateTime.now());
        oldLikeRecord.setTargetUserId(targetApp.getUserId());
        if (isLiked == 1) {
            oldLikeRecord.setIsRead(0);
        }
        boolean updated = this.updateById(oldLikeRecord);
        ThrowUtil.throwIf(!updated, ErrorCode.OPERATION_ERROR, "failed to update like record");
        updateLikeCount(targetApp, isLiked == 1 ? 1 : -1);
        return true;
    }

    @Override
    public List<LikeRecordVO> listUnreadLikes(String userId, int limit) {
        int finalLimit = Math.max(1, limit);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
                .eq("is_read", 0)
                .eq("is_liked", 1)
                .ne("user_id", userId)
                .orderBy("last_like_time", false)
                .limit(finalLimit);
        List<LikeRecord> unreadLikes = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadLikes)) {
            return new ArrayList<>();
        }
        return convertToVOList(unreadLikes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markLikesAsRead(String userId, List<String> likeIds) {
        if (StrUtil.isBlank(userId) || CollUtil.isEmpty(likeIds)) {
            return;
        }
        UpdateChain.of(LikeRecord.class)
                .setRaw(LikeRecord::getIsRead, 1)
                .where(LikeRecord::getTargetUserId).eq(userId)
                .and(LikeRecord::getLikeId).in(likeIds)
                .and(LikeRecord::getIsRead).eq(0)
                .and(LikeRecord::getIsLiked).eq(1)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<LikeRecordVO> getAndClearUnreadLikes(String userId) {
        List<LikeRecordVO> unreadLikes = listUnreadLikes(userId, 50);
        if (CollUtil.isEmpty(unreadLikes)) {
            return new ArrayList<>();
        }
        markLikesAsRead(userId, unreadLikes.stream().map(LikeRecordVO::getLikeId).collect(Collectors.toList()));
        return unreadLikes;
    }

    @Override
    public Page<LikeRecordVO> getUserLikeHistory(LikeQueryRequest likeQueryRequest, String userId) {
        long current = likeQueryRequest.getCurrent();
        long size = likeQueryRequest.getPageSize();
        Page<LikeRecord> page = new Page<>(current, size);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
                .eq("is_liked", 1)
                .ne("user_id", userId)
                .orderBy("last_like_time", false);
        Page<LikeRecord> likePage = this.page(page, queryWrapper);
        List<LikeRecordVO> records = convertToVOList(likePage.getRecords());
        Page<LikeRecordVO> voPage = new Page<>(likePage.getPageNumber(), likePage.getPageSize(), likePage.getTotalRow());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public boolean isContentLiked(String targetId, String userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_id", targetId)
                .eq("user_id", userId)
                .eq("is_liked", 1);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public long getUnreadLikesCount(String userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
                .eq("is_read", 0)
                .eq("is_liked", 1)
                .ne("user_id", userId);
        return this.count(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllUnreadLikes(String userId) {
        UpdateChain.of(LikeRecord.class)
                .setRaw(LikeRecord::getIsRead, 1)
                .where(LikeRecord::getTargetUserId).eq(userId)
                .and(LikeRecord::getIsRead).eq(0)
                .and(LikeRecord::getIsLiked).eq(1)
                .update();
    }

    @Override
    public Page<LikeRecordVO> getMyLikeHistory(LikeQueryRequest likeQueryRequest, String userId) {
        long current = likeQueryRequest.getCurrent();
        long size = likeQueryRequest.getPageSize();
        Page<LikeRecord> page = new Page<>(current, size);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId)
                .eq("is_liked", 1)
                .orderBy("last_like_time", false);
        Page<LikeRecord> likePage = this.page(page, queryWrapper);
        List<LikeRecordVO> records = convertToVOList(likePage.getRecords());
        Page<LikeRecordVO> voPage = new Page<>(likePage.getPageNumber(), likePage.getPageSize(), likePage.getTotalRow());
        voPage.setRecords(records);
        return voPage;
    }

    private void updateLikeCount(App app, int delta) {
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "app not found");
        long currentLikeCount = app.getLikeCount() == null ? 0L : app.getLikeCount();
        app.setLikeCount(Math.max(0L, currentLikeCount + delta));
        boolean updated = appService.updateById(app);
        ThrowUtil.throwIf(!updated, ErrorCode.OPERATION_ERROR, "failed to update app like count");
    }

    private List<LikeRecordVO> convertToVOList(List<LikeRecord> likeRecords) {
        if (CollUtil.isEmpty(likeRecords)) {
            return new ArrayList<>();
        }
        return likeRecords.stream().map(like -> {
            LikeRecordVO likeRecordVO = new LikeRecordVO();
            BeanUtils.copyProperties(like, likeRecordVO);
            User likeUser = userService.getById(like.getUserId());
            if (likeUser != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(likeUser, userVO);
                likeRecordVO.setUserVO(userVO);
            }
            App app = appService.getById(like.getTargetId());
            if (app != null) {
                AppVO appVO = new AppVO();
                BeanUtils.copyProperties(app, appVO);
                likeRecordVO.setAppVO(appVO);
            }
            return likeRecordVO;
        }).collect(Collectors.toList());
    }
}
