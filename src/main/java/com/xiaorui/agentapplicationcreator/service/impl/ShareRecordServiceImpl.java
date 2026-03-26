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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 分享记录表 服务层实现
 *
 * @author xiaorui
 */
@Slf4j
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
    @Async("appLikeOrShareExecutor")
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<Boolean> doShare(ShareDoRequest shareDoRequest, String userId) {
        try {
            String targetId = shareDoRequest.getTargetId();
            int isShared = shareDoRequest.getIsShared();
            if (StrUtil.isBlank(targetId)) {
                log.error("targetId is empty: targetId={}", targetId);
                return CompletableFuture.completedFuture(false);
            }
            String targetUserId = getTargetUserId(targetId);
            if (targetUserId == null) {
                log.error("Share content not found: targetId={}", targetId);
                return CompletableFuture.completedFuture(false);
            }
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_id", userId).eq("target_id", targetId);
            ShareRecord oldShareRecord = this.getOne(queryWrapper);
            if (oldShareRecord == null) {
                if (isShared == 1) {
                    ShareRecord shareRecord = new ShareRecord();
                    shareRecord.setUserId(userId);
                    shareRecord.setTargetId(targetId);
                    shareRecord.setTargetUserId(targetUserId);
                    shareRecord.setIsShared(isShared);
                    shareRecord.setCreateTime(LocalDateTime.now());
                    shareRecord.setShareTime(LocalDateTime.now());
                    shareRecord.setIsRead(0);
                    this.save(shareRecord);
                    updateShareCount(targetId, 1);
                }
            } else if (isShared != oldShareRecord.getIsShared()) {
                oldShareRecord.setIsShared(isShared);
                oldShareRecord.setUpdateTime(LocalDateTime.now());
                oldShareRecord.setTargetUserId(targetUserId);
                if (isShared == 1) {
                    oldShareRecord.setIsRead(0);
                    oldShareRecord.setShareTime(LocalDateTime.now());
                }
                this.updateById(oldShareRecord);
                updateShareCount(targetId, isShared == 1 ? 1 : -1);
            }
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Error in doShare: ", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ShareRecordVO> getAndClearUnreadShares(String userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
                .eq("is_read", 0)
                .eq("is_shared", 1)
                .ne("user_id", userId)
                .orderBy("share_time", false)
                .limit(50);
        List<ShareRecord> unreadShares = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadShares)) {
            return new ArrayList<>();
        }
        List<String> shareIds = unreadShares.stream().map(ShareRecord::getShareId).toList();
        UpdateChain.of(ShareRecord.class)
                .setRaw(ShareRecord::getIsRead, 1)
                .where(ShareRecord::getShareId).in(shareIds)
                .update();
        return convertToVOList(unreadShares);
    }

    @Override
    public Page<ShareRecordVO> getUserShareHistory(ShareQueryRequest shareQueryRequest, String userId) {
        long current = shareQueryRequest.getCurrent();
        long size = shareQueryRequest.getPageSize();
        Page<ShareRecord> page = new Page<>(current, size);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
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
    public void clearAllUnreadShares(String userId) {
        UpdateChain.of(ShareRecord.class)
                .setRaw(ShareRecord::getIsRead, 1)
                .where(ShareRecord::getTargetUserId).eq(userId)
                .and(ShareRecord::getIsRead).eq(0)
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
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        String shareUrl = resolveShareUrl(app);
        SharePreviewVO sharePreviewVO = new SharePreviewVO();
        sharePreviewVO.setAppId(app.getAppId());
        sharePreviewVO.setAppName(app.getAppName());
        sharePreviewVO.setShareUrl(shareUrl);
        sharePreviewVO.setQrCodeDataUrl(buildQrCodeDataUrl(shareUrl));
        return sharePreviewVO;
    }

    private String getTargetUserId(String targetId) {
        try {
            App app = appService.getById(targetId);
            return app == null ? null : app.getUserId();
        } catch (Exception e) {
            log.error("Error getting target user id: ", e);
            return null;
        }
    }

    private void updateShareCount(String targetId, int delta) {
        App app = appService.getById(targetId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        long currentShareCount = app.getShareCount() == null ? 0L : app.getShareCount();
        app.setShareCount(Math.max(0L, currentShareCount + delta));
        appService.updateById(app);
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
