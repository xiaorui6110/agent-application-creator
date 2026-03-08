package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 点赞记录表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord>  implements LikeRecordService{

    @Lazy
    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 通用点赞/取消点赞
     *
     * @param likeDoRequest 点赞请求
     * @param userId 用户ID
     * @return 点赞结果
     */
    @Override
    @Async("appLikeOrShareExecutor")
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<Boolean> doLike(LikeDoRequest likeDoRequest, String userId) {
        try {
            String targetId = likeDoRequest.getTargetId();
            int isLiked = likeDoRequest.getIsLiked();
            // 参数校验
            if (StrUtil.isBlank(targetId)) {
                log.error("Target ID is empty: targetId={}", targetId);
                return CompletableFuture.completedFuture(false);
            }
            // 获取目标内容所属用户ID
            String targetUserId = getTargetUserId(targetId);
            if (targetUserId == null) {
                log.error("Target content not found: targetId={}", targetId);
                return CompletableFuture.completedFuture(false);
            }
            // 查询当前点赞状态
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_id", userId)
                    .eq("target_id", targetId);
            LikeRecord oldLikeRecord = this.getOne(queryWrapper);
            // 处理点赞记录
            if (oldLikeRecord == null) {
                if (isLiked == 1) {
                    // 首次点赞
                    LikeRecord likeRecord = new LikeRecord();
                    likeRecord.setUserId(userId);
                    likeRecord.setTargetId(targetId);
                    // 设置目标内容所属用户ID
                    likeRecord.setTargetUserId(targetUserId);
                    likeRecord.setIsLiked(isLiked);
                    likeRecord.setFirstLikeTime(LocalDateTime.now());
                    likeRecord.setLastLikeTime(LocalDateTime.now());
                    // 未读状态
                    likeRecord.setIsRead(0);
                    this.save(likeRecord);
                    updateLikeCount(targetId, 1);
                }
            } else {
                if (isLiked != oldLikeRecord.getIsLiked()) {
                    // 更新点赞状态
                    oldLikeRecord.setIsLiked(isLiked);
                    oldLikeRecord.setLastLikeTime(LocalDateTime.now());
                    // 更新目标内容所属用户ID
                    oldLikeRecord.setTargetUserId(targetUserId);
                    // 如果是重新点赞，设置为未读
                    if (isLiked == 1) {
                        oldLikeRecord.setIsRead(0);
                    }
                    this.updateById(oldLikeRecord);
                    updateLikeCount(targetId, isLiked == 1 ? 1 : -1);
                }
            }
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Error in doLike: ", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 获取并清除用户未读的点赞消息
     *
     * @param userId 用户ID
     * @return 点赞消息列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<LikeRecordVO> getAndClearUnreadLikes(String userId) {
        // 1. 获取未读点赞记录
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
                .eq("is_read", 0)
                .eq("is_liked", 1)
                .ne("target_user_id", userId)
                .orderBy("last_like_time", false)
                // 限制最多返回50条数据
                .limit(50);
        List<LikeRecord> unreadLikes = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadLikes)) {
            return new ArrayList<>();
        }
        // 2. 批量更新为已读
        List<String> likeIds = unreadLikes.stream()
                .map(LikeRecord::getLikeId)
                .collect(Collectors.toList());
        UpdateChain.of(LikeRecord.class)
                .setRaw(LikeRecord::getIsRead, 1)
                .where(LikeRecord::getLikeId).eq(likeIds)
                .update();
        return convertToVOList(unreadLikes);
    }


    /**
     * 获取用户的点赞历史（分页）
     *
     * @param likeQueryRequest 点赞查询请求
     * @param userId 用户ID
     * @return 点赞历史列表
     */
    @Override
    public Page<LikeRecordVO> getUserLikeHistory(LikeQueryRequest likeQueryRequest, String userId) {
        long current = likeQueryRequest.getCurrent();
        long size = likeQueryRequest.getPageSize();
        // 创建分页对象
        Page<LikeRecord> page = new Page<>(current, size);
        // 构建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        // 查询被点赞的记录
        queryWrapper.eq("target_user_id", userId)
                // 排除自己点赞自己的记录;
                .ne("user_id", userId)
                .orderBy("last_like_time", false);
        // 执行分页查询
        Page<LikeRecord> likePage = this.page(page, queryWrapper);
        // 转换结果
        List<LikeRecordVO> records = convertToVOList(likePage.getRecords());
        // 构建返回结果
        Page<LikeRecordVO> voPage = new Page<>(likePage.getPageNumber(), likePage.getPageSize(), likePage.getTotalRow());
        voPage.setRecords(records);
        return voPage;
    }

    /**
     * 检查内容是否已被用户点赞
     *
     * @param targetId 目标ID
     * @param userId 用户ID
     * @return 是否已被点赞
     */
    @Override
    public boolean isContentLiked(String targetId, String userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_id", targetId)
                .eq("user_id", userId)
                .eq("is_liked", 1);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 获取用户未读点赞数
     *
     * @param userId 用户ID
     * @return 未读点赞数
     */
    @Override
    public long getUnreadLikesCount(String userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("target_user_id", userId)
                .eq("is_read", 0)
                .eq("is_liked", 1)
                .ne("target_user_id", userId);
        return this.count(queryWrapper);
    }

    /**
     * 清除用户所有未读点赞状态
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllUnreadLikes(String userId) {
        UpdateChain.of(LikeRecord.class)
                .setRaw(LikeRecord::getIsRead, 1)
                .where(LikeRecord::getTargetUserId).eq(userId)
                .and(LikeRecord::getIsRead).eq(0)
                .update();
    }

    /**
     * 获取用户自己的点赞历史（分页）
     *
     * @param likeQueryRequest 点赞查询请求
     * @param userId 用户ID
     * @return 点赞历史列表
     */
    @Override
    public Page<LikeRecordVO> getMyLikeHistory(LikeQueryRequest likeQueryRequest, String userId) {
        long current = likeQueryRequest.getCurrent();
        long size = likeQueryRequest.getPageSize();
        // 创建分页对象
        Page<LikeRecord> page = new Page<>(current, size);
        // 构建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        // 查询用户自己的点赞记录
        queryWrapper.eq("user_id", userId)
                // 只查询点赞状态为1的记录
                .eq("is_liked", 1)
                .orderBy("last_like_time", false);
        // 执行分页查询
        Page<LikeRecord> likePage = this.page(page, queryWrapper);
        // 转换结果
        List<LikeRecordVO> records = convertToVOList(likePage.getRecords());
        // 构建返回结果
        Page<LikeRecordVO> voPage = new Page<>(likePage.getPageNumber(), likePage.getPageSize(), likePage.getTotalRow());
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
     * 更新点赞数
     *
     * @param targetId 目标ID
     * @param delta 点赞数变化量（两种情况：点赞/取消点赞）
     */
    private void updateLikeCount(String targetId, int delta) {
        App app = appService.getById(targetId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        }
        app.setLikeCount(app.getLikeCount() + delta);
        appService.updateById(app);
    }

    /**
     * 将点赞记录列表转换为VO列表
     *
     * @param likeRecords 点赞记录列表
     * @return 点赞记录VO列表
     */
    private List<LikeRecordVO> convertToVOList(List<LikeRecord> likeRecords) {
        if (CollUtil.isEmpty(likeRecords)) {
            return new ArrayList<>();
        }
        return likeRecords.stream().map(like -> {
            LikeRecordVO likeRecordVO = new LikeRecordVO();
            BeanUtils.copyProperties(like, likeRecordVO);
            // 设置点赞用户信息
            User likeUser = userService.getById(like.getUserId());
            if (likeUser != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(likeUser, userVO);
                likeRecordVO.setUserVO(userVO);
            }
            // 设置应用信息
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
