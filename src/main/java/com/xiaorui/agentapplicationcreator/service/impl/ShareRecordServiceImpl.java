package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.config.properties.AppProperties;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.ShareRecordMapper;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareDoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.ShareRecord;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.AppVO;
import com.xiaorui.agentapplicationcreator.model.vo.SharePreviewVO;
import com.xiaorui.agentapplicationcreator.model.vo.ShareRecordVO;
import com.xiaorui.agentapplicationcreator.model.vo.UserVO;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.ShareRecordService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import jakarta.annotation.Resource;
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
@Service
public class ShareRecordServiceImpl extends ServiceImpl<ShareRecordMapper, ShareRecord> implements ShareRecordService {

    @Lazy
    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private AppProperties appProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doShare(ShareDoRequest shareDoRequest, String userId) {
        ThrowUtil.throwIf(shareDoRequest == null, ErrorCode.PARAMS_ERROR, "share request is null");
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_LOGIN_ERROR, "user not logged in");

        String targetId = shareDoRequest.getTargetId();
        int isShared = shareDoRequest.getIsShared();
        ThrowUtil.throwIf(StrUtil.isBlank(targetId), ErrorCode.PARAMS_ERROR, "targetId is blank");
        ThrowUtil.throwIf(isShared != 0 && isShared != 1, ErrorCode.PARAMS_ERROR, "isShared must be 0 or 1");

        User loginUser = userService.getById(userId);
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "user not found");
        App targetApp = appService.getById(targetId);
        ThrowUtil.throwIf(targetApp == null, ErrorCode.NOT_FOUND_ERROR, "target app not found");

        ShareRecord oldShareRecord = this.getOne(new QueryWrapper()
                .eq("user_id", userId)
                .eq("target_id", targetId));
        if (oldShareRecord == null) {
            if (isShared == 0) {
                return true;
            }
            ShareRecord shareRecord = new ShareRecord();
            shareRecord.setUserId(userId);
            shareRecord.setTargetId(targetId);
            shareRecord.setTargetUserId(targetApp.getUserId());
            shareRecord.setIsShared(1);
            shareRecord.setCreateTime(LocalDateTime.now());
            shareRecord.setShareTime(LocalDateTime.now());
            shareRecord.setIsRead(0);
            boolean saved = this.save(shareRecord);
            ThrowUtil.throwIf(!saved, ErrorCode.OPERATION_ERROR, "failed to create share record");
            updateShareCount(targetApp, 1);
            return true;
        }

        if (isShared == oldShareRecord.getIsShared()) {
            return true;
        }
        oldShareRecord.setIsShared(isShared);
        oldShareRecord.setUpdateTime(LocalDateTime.now());
        oldShareRecord.setTargetUserId(targetApp.getUserId());
        if (isShared == 1) {
            oldShareRecord.setIsRead(0);
            oldShareRecord.setShareTime(LocalDateTime.now());
        }
        boolean updated = this.updateById(oldShareRecord);
        ThrowUtil.throwIf(!updated, ErrorCode.OPERATION_ERROR, "failed to update share record");
        updateShareCount(targetApp, isShared == 1 ? 1 : -1);
        return true;
    }

    @Override
    public List<ShareRecordVO> listUnreadShares(String userId, int limit) {
        int finalLimit = Math.max(1, limit);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
                .eq("is_read", 0)
                .eq("is_shared", 1)
                .ne("user_id", userId)
                .orderBy("share_time", false)
                .limit(finalLimit);
        List<ShareRecord> unreadShares = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadShares)) {
            return new ArrayList<>();
        }
        return convertToVOList(unreadShares);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSharesAsRead(String userId, List<String> shareIds) {
        if (StrUtil.isBlank(userId) || CollUtil.isEmpty(shareIds)) {
            return;
        }
        UpdateChain.of(ShareRecord.class)
                .setRaw(ShareRecord::getIsRead, 1)
                .where(ShareRecord::getTargetUserId).eq(userId)
                .and(ShareRecord::getShareId).in(shareIds)
                .and(ShareRecord::getIsRead).eq(0)
                .and(ShareRecord::getIsShared).eq(1)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ShareRecordVO> getAndClearUnreadShares(String userId) {
        List<ShareRecordVO> unreadShares = listUnreadShares(userId, 50);
        if (CollUtil.isEmpty(unreadShares)) {
            return new ArrayList<>();
        }
        markSharesAsRead(userId, unreadShares.stream().map(ShareRecordVO::getShareId).toList());
        return unreadShares;
    }

    @Override
    public Page<ShareRecordVO> getUserShareHistory(ShareQueryRequest shareQueryRequest, String userId) {
        long current = shareQueryRequest.getCurrent();
        long size = shareQueryRequest.getPageSize();
        Page<ShareRecord> page = new Page<>(current, size);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
                .eq("is_shared", 1)
                .ne("user_id", userId)
                .orderBy("share_time", false);
        Page<ShareRecord> sharePage = this.page(page, queryWrapper);
        List<ShareRecordVO> records = convertToVOList(sharePage.getRecords());
        Page<ShareRecordVO> voPage = new Page<>(sharePage.getPageNumber(), sharePage.getPageSize(), sharePage.getTotalRow());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public boolean isContentShared(String targetId, String userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_id", targetId)
                .eq("user_id", userId)
                .eq("is_shared", 1);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public long getUnreadSharesCount(String userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
                .eq("is_read", 0)
                .eq("is_shared", 1)
                .ne("user_id", userId);
        return this.count(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllUnreadShares(String userId) {
        UpdateChain.of(ShareRecord.class)
                .setRaw(ShareRecord::getIsRead, 1)
                .where(ShareRecord::getTargetUserId).eq(userId)
                .and(ShareRecord::getIsRead).eq(0)
                .and(ShareRecord::getIsShared).eq(1)
                .update();
    }

    @Override
    public Page<ShareRecordVO> getMyShareHistory(ShareQueryRequest shareQueryRequest, String userId) {
        long current = shareQueryRequest.getCurrent();
        long size = shareQueryRequest.getPageSize();
        Page<ShareRecord> page = new Page<>(current, size);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId)
                .eq("is_shared", 1)
                .orderBy("share_time", false);
        Page<ShareRecord> sharePage = this.page(page, queryWrapper);
        List<ShareRecordVO> records = convertToVOList(sharePage.getRecords());
        Page<ShareRecordVO> voPage = new Page<>(sharePage.getPageNumber(), sharePage.getPageSize(), sharePage.getTotalRow());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public SharePreviewVO getSharePreview(String targetId) {
        App app = appService.getById(targetId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "app not found");
        }
        String shareUrl = resolveShareUrl(app);
        SharePreviewVO sharePreviewVO = new SharePreviewVO();
        sharePreviewVO.setAppId(app.getAppId());
        sharePreviewVO.setAppName(app.getAppName());
        sharePreviewVO.setShareUrl(shareUrl);
        sharePreviewVO.setQrCodeDataUrl(buildQrCodeDataUrl(shareUrl));
        return sharePreviewVO;
    }

    private void updateShareCount(App app, int delta) {
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "app not found");
        long currentShareCount = app.getShareCount() == null ? 0L : app.getShareCount();
        app.setShareCount(Math.max(0L, currentShareCount + delta));
        boolean updated = appService.updateById(app);
        ThrowUtil.throwIf(!updated, ErrorCode.OPERATION_ERROR, "failed to update app share count");
    }

    private String resolveShareUrl(App app) {
        if (StrUtil.isNotBlank(app.getDeployUrl())) {
            return app.getDeployUrl();
        }
        if (StrUtil.isNotBlank(app.getDeployKey())) {
            return appProperties.buildDeployUrl(app.getDeployKey());
        }
        return appProperties.getDeploy().getPublicBaseUrl().replaceAll("/+$", "") + "/app/chat/" + app.getAppId();
    }

    private String buildQrCodeDataUrl(String content) {
        byte[] qrCodeBytes = QrCodeUtil.generatePng(content, 280, 280);
        return "data:image/png;base64," + Base64.encode(qrCodeBytes);
    }

    private List<ShareRecordVO> convertToVOList(List<ShareRecord> shareRecords) {
        if (CollUtil.isEmpty(shareRecords)) {
            return new ArrayList<>();
        }
        return shareRecords.stream().map(share -> {
            ShareRecordVO shareRecordVO = new ShareRecordVO();
            BeanUtils.copyProperties(share, shareRecordVO);
            User shareUser = userService.getById(share.getUserId());
            if (shareUser != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(shareUser, userVO);
                shareRecordVO.setUserVO(userVO);
            }
            App app = appService.getById(share.getTargetId());
            if (app != null) {
                AppVO appVO = new AppVO();
                BeanUtils.copyProperties(app, appVO);
                shareRecordVO.setAppVO(appVO);
            }
            return shareRecordVO;
        }).collect(Collectors.toList());
    }
}
