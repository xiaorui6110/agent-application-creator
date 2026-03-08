package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.mapper.ShareRecordMapper;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareDoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.ShareRecord;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.AppVO;
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
 * 分享记录表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class ShareRecordServiceImpl extends ServiceImpl<ShareRecordMapper, ShareRecord>  implements ShareRecordService{

    @Lazy
    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 分享
     *
     * @param shareDoRequest 分享请求
     * @param userId 用户ID
     * @return 分享结果
     */
    @Override
    @Async("appLikeOrShareExecutor")
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<Boolean> doShare(ShareDoRequest shareDoRequest, String userId) {
        try {
            String targetId = shareDoRequest.getTargetId();
            int isShared = shareDoRequest.getIsShared();
            // 参数校验
            if (StrUtil.isBlank(targetId)) {
                log.error("targetId is empty: targetId={}", targetId);
                return CompletableFuture.completedFuture(false);
            }
            // 获取目标内容所属用户ID
            String targetUserId = getTargetUserId(targetId);
            if (targetUserId == null) {
                log.error("Share content not found: targetId={}", targetId);
                return CompletableFuture.completedFuture(false);
            }
            // 查询当前分享状态
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_id", userId)
                    .eq("target_id", targetId);
            ShareRecord oldShareRecord = this.getOne(queryWrapper);
            // 处理分享记录
            if (oldShareRecord == null) {
                if (isShared == 1) {
                    // 首次分享
                    ShareRecord shareRecord = new ShareRecord();
                    shareRecord.setUserId(userId);
                    shareRecord.setTargetId(targetId);
                    // 设置目标内容所属用户ID
                    shareRecord.setTargetUserId(targetUserId);
                    shareRecord.setIsShared(isShared);
                    shareRecord.setCreateTime(LocalDateTime.now());
                    shareRecord.setShareTime(LocalDateTime.now());
                    // 未读状态
                    shareRecord.setIsRead(0);
                    this.save(shareRecord);
                    updateLikeCount(targetId, 1);
                }
            } else {
                if (isShared != oldShareRecord.getIsShared()) {
                    // 更新分享状态
                    oldShareRecord.setIsShared(isShared);
                    oldShareRecord.setUpdateTime(LocalDateTime.now());
                    // 更新目标内容所属用户ID
                    oldShareRecord.setTargetUserId(targetUserId);
                    // 如果是重新分享，设置为未读
                    if (isShared == 1) {
                        oldShareRecord.setIsRead(0);
                    }
                    this.updateById(oldShareRecord);
                    updateLikeCount(targetId, isShared == 1 ? 1 : -1);
                }
            }
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Error in doShare: ", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 获取并清除用户未读的分享消息
     *
     * @param userId 用户ID
     * @return 分享记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ShareRecordVO> getAndClearUnreadShares(String userId) {
        // 1. 获取未读点赞记录
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
                .eq("is_read", 0)
                .eq("is_shared", 1)
                .ne("target_user_id", userId)
                .orderBy("share_time", false)
                // 限制最多返回50条数据
                .limit(50);
        List<ShareRecord> unreadShares = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadShares)) {
            return new ArrayList<>();
        }
        // 2. 批量更新为已读
        List<String> shareIds = unreadShares.stream()
                .map(ShareRecord::getShareId)
                .collect(Collectors.toList());
        UpdateChain.of(ShareRecord.class)
                .setRaw(ShareRecord::getIsRead, 1)
                .where(ShareRecord::getShareId).eq(shareIds)
                .update();
        return convertToVOList(unreadShares);
    }

    /**
     * 获取用户分享历史（分页）
     *
     * @param shareQueryRequest 查询分享请求
     * @param userId 用户ID
     * @return 分享记录
     */
    @Override
    public Page<ShareRecordVO> getUserShareHistory(ShareQueryRequest shareQueryRequest, String userId) {
        long current = shareQueryRequest.getCurrent();
        long size = shareQueryRequest.getPageSize();
        // 创建分页对象
        Page<ShareRecord> page = new Page<>(current, size);
        // 构建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        // 查询被点赞的记录
        queryWrapper.eq("target_user_id", userId)
                // 排除自己点赞自己的记录;
                .ne("user_id", userId)
                .orderBy("share_time", false);
        // 执行分页查询
        Page<ShareRecord> sharePage = this.page(page, queryWrapper);
        // 转换结果
        List<ShareRecordVO> records = convertToVOList(sharePage.getRecords());
        // 构建返回结果
        Page<ShareRecordVO> voPage = new Page<>(sharePage.getPageNumber(), sharePage.getPageSize(), sharePage.getTotalRow());
        voPage.setRecords(records);
        return voPage;
    }

    /**
     * 判断内容是否被分享过
     *
     * @param targetId 目标ID
     * @param userId 用户ID
     * @return 是否被分享过
     */
    @Override
    public boolean isContentShared(String targetId, String userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_id", targetId)
                .eq("user_id", userId)
                .eq("is_shared", 1);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 获取用户未读分享数量
     *
     * @param userId 用户ID
     * @return 未读分享数量
     */
    @Override
    public long getUnreadSharesCount(String userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
                .eq("is_read", 0)
                .eq("is_shared", 1)
                .ne("target_user_id", userId);
        return this.count(queryWrapper);
    }

    /**
     * 清除用户所有未读分享状态
     *
     * @param userId 用户ID
     */
    @Override
    public void clearAllUnreadShares(String userId) {
        UpdateChain.of(ShareRecord.class)
                .setRaw(ShareRecord::getIsRead, 1)
                .where(ShareRecord::getTargetUserId).eq(userId)
                .and(ShareRecord::getIsRead).eq(0)
                .update();
    }

    /**
     * 获取用户自己的分享历史（分页）
     *
     * @param shareQueryRequest 查询分享请求
     * @param userId 用户ID
     * @return 分享记录
     */
    @Override
    public Page<ShareRecordVO> getMyShareHistory(ShareQueryRequest shareQueryRequest, String userId) {
        long current = shareQueryRequest.getCurrent();
        long size = shareQueryRequest.getPageSize();
        // 创建分页对象
        Page<ShareRecord> page = new Page<>(current, size);
        // 构建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        // 查询用户自己的点赞记录
        queryWrapper.eq("user_id", userId)
                // 只查询点赞状态为1的记录
                .eq("is_shared", 1)
                .orderBy("share_time", false);
        // 执行分页查询
        Page<ShareRecord> sharePage = this.page(page, queryWrapper);
        // 转换结果
        List<ShareRecordVO> records = convertToVOList(sharePage.getRecords());
        // 构建返回结果
        Page<ShareRecordVO> voPage = new Page<>(sharePage.getPageNumber(), sharePage.getPageSize(), sharePage.getTotalRow());
        voPage.setRecords(records);
        return voPage;
    }

    /**
     * 获取目标内容所属用户ID
     *
     * @param targetId 目标ID
     * @return 目标内容所属用户ID
     */
    private String getTargetUserId(String targetId) {
        try {
            return appService.getById(targetId).getUserId();
        } catch (Exception e) {
            log.error("Error getting target user id: ", e);
            return null;
        }
    }

    /**
     * 更新分享数
     *
     * @param targetId 目标ID
     * @param delta 分享数变化量（两种情况：分享/取消分享）
     */
    private void updateLikeCount(String targetId, int delta) {
        App app = appService.getById(targetId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        }
        app.setShareCount(app.getShareCount() + delta);
        appService.updateById(app);
    }

    /**
     * 将分享记录列表转换为VO列表
     *
     * @param shareRecords 分享记录列表
     * @return 分享记录VO列表
     */
    private List<ShareRecordVO> convertToVOList(List<ShareRecord> shareRecords) {
        if (CollUtil.isEmpty(shareRecords)) {
            return new ArrayList<>();
        }
        return shareRecords.stream().map(share -> {
            ShareRecordVO shareRecordVO = new ShareRecordVO();
            BeanUtils.copyProperties(share, shareRecordVO);
            // 设置点赞用户信息
            User shareUser = userService.getById(share.getUserId());
            if (shareUser != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(shareUser, userVO);
                shareRecordVO.setUserVO(userVO);
            }
            // 设置应用信息
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
