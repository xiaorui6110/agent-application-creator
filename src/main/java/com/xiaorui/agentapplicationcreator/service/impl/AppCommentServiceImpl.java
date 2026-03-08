package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
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
import org.springframework.beans.BeanUtils;
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
 * 应用评论表 服务层实现。
 *
 * @author xiaorui
 */
@Service
public class AppCommentServiceImpl extends ServiceImpl<AppCommentMapper, AppComment>  implements AppCommentService{

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    /**
     * 添加评论
     *
     * @param appCommentAddRequest 添加评论请求
     * @return 添加评论结果
     */
    @Override
    public Boolean addAppComment(AppCommentAddRequest appCommentAddRequest) {
        // 获取当前用户信息
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }
        // 校验应用是否存在
        App app = appService.getById(appCommentAddRequest.getAppId());
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        }
        // 评论不能为空
        if (StrUtil.isBlank(appCommentAddRequest.getCommentContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"评论不能为空");
        }
        // 获取评论的应用所属用户id
        String appUserId = app.getUserId();
        // 创建评论
        AppComment appComment = new AppComment();
        appComment.setUserId(userId);
        appComment.setAppId(appCommentAddRequest.getAppId());
        appComment.setAppUserId(appUserId);
        appComment.setCommentContent(appCommentAddRequest.getCommentContent());
        appComment.setCreateTime(LocalDateTime.now());
        // 设置初始点赞数等等
        appComment.setLikeCount(0L);
        appComment.setDislikeCount(0L);
        appComment.setIsDeleted(0);
        appComment.setIsRead(0);
        boolean result = save(appComment);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评论保存失败");
        }
        // 更新评论数 +1
        updateCommentCount(appCommentAddRequest.getAppId());
        return true;
    }

    /**
     * 删除评论
     *
     * @param appCommentDeleteRequest 删除评论请求
     * @return 删除评论结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAppComment(AppCommentDeleteRequest appCommentDeleteRequest) {
        // 获取当前用户信息
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }
        // 查询评论信息
        AppComment appComment = getById(appCommentDeleteRequest.getCommentId());
        if (appComment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"评论不存在");
        }
        // 判断是否是本人（只能删除自己的评论）
        if (!appComment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR,"无权删除他人评论");
        }
        // 先计算要删除的评论及其子评论的总数
        int totalCount = countCommentsRecursively(appComment.getCommentId());
        // 删除评论及其子评论
        boolean result = deleteCommentsRecursively(appComment.getCommentId());
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评论删除失败");
        }
        // 更新 MySQL 中的评论数
        App app = appService.getById(appComment.getAppId());
        app.setCommentCount(app.getCommentCount() - totalCount);
        appService.updateById(app);
        return true;
    }

    /**
     * 查询评论
     *
     * @param appCommentQueryRequest 查询评论请求
     * @return 查询评论结果
     */
    @Override
    public Page<AppCommentVO> queryAppComment(AppCommentQueryRequest appCommentQueryRequest) {
        // 获取当前用户信息
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }
        int pageSize = appCommentQueryRequest.getPageSize();
        int current = appCommentQueryRequest.getCurrent();
        Page<AppComment> page = new Page<>(current, pageSize);
        ThrowUtil.throwIf(appCommentQueryRequest.getAppId() == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        // 查询顶级评论
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("app_id", appCommentQueryRequest.getAppId());
        queryWrapper.eq("parent_id", 0);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderBy("create_time", false);
        // 查询评论是否存在，不存在返回空
        List<AppComment> appComments = page.getRecords();
        if (CollectionUtil.isEmpty(appComments)) {
            return new Page<>();
        }
        // 得到顶级评论列表
        Page<AppComment> appCommentPage = page(page, queryWrapper);
        List<String> userIds = appComments.stream().map(AppComment::getUserId).toList();
        // 批量查询评论用户信息，先检查 userIds 不为空
        if (CollectionUtil.isEmpty(userIds)) {
            return new Page<>(appCommentPage.getPageNumber(), appCommentPage.getPageSize(), appCommentPage.getTotalRow());
        }
        List<User> users = userService.listByIds(userIds);
        // 将 User 列表转换为 AppCommentUserVO 列表
        List<AppCommentUserVO> appCommentUserVOList = users.stream().map(user -> {
            AppCommentUserVO appCommentUserVO = new AppCommentUserVO();
            BeanUtil.copyProperties(user, appCommentUserVO);
            return appCommentUserVO;
        }).toList();
        // 将 AppComment 列表转换为 AppCommentVO 列表
        Map<String, AppCommentUserVO> userMap = appCommentUserVOList.stream()
                .collect(Collectors.toMap(AppCommentUserVO::getUserId, AppCommentUserVO -> AppCommentUserVO));
        List<AppCommentVO> commentsVOList = appCommentPage.getRecords().stream().map(appComment -> {
            AppCommentVO appCommentVO = new AppCommentVO();
            BeanUtils.copyProperties(appComments, appCommentVO);
            // 查找对应的评论用户信息
            AppCommentUserVO appCommentUserVO = userMap.get(appComment.getUserId());
            if (appCommentUserVO != null) {
                appCommentVO.setAppCommentUserVO(appCommentUserVO);
            }
            // 递归查询子评论
            appCommentVO.setChildCommentList(getChildrenComments(appComment.getCommentId()));
            return appCommentVO;
        }).toList();
        Page<AppCommentVO> appCommentVOPage = new Page<>(
                appCommentPage.getPageNumber(), appCommentPage.getPageSize(), appCommentPage.getTotalRow());
        appCommentVOPage.setRecords(commentsVOList);
        return appCommentVOPage;
    }


    /**
     * 点赞评论
     *
     * @param appCommentLikeRequest 点赞评论请求
     * @return 点赞评论结果
     */
    @Override
    public Boolean likeAppComment(AppCommentLikeRequest appCommentLikeRequest) {
        ThrowUtil.throwIf(appCommentLikeRequest.getCommentId() == null, ErrorCode.PARAMS_ERROR, "评论ID不能为空");
        ThrowUtil.throwIf(appCommentLikeRequest.getLikeType() == null, ErrorCode.PARAMS_ERROR, "点赞类型不能为空");
        // 点赞
        if (appCommentLikeRequest.getLikeType() == LIKE || appCommentLikeRequest.getLikeCount() != 0) {
            UpdateChain.of(AppComment.class)
                    .setRaw(AppComment::getLikeCount, "like_count + 1")
                    .where(AppComment::getCommentId).eq(appCommentLikeRequest.getCommentId())
                    .update();
        }
        // 取消点赞
        if (appCommentLikeRequest.getLikeType() == CANCEL_LIKE) {
            UpdateChain.of(AppComment.class)
                    .setRaw(AppComment::getLikeCount, "like_count - 1")
                    .where(AppComment::getCommentId).eq(appCommentLikeRequest.getCommentId())
                    .update();
        }
        // 点踩
        if (appCommentLikeRequest.getDislikeCount() != null && appCommentLikeRequest.getDislikeCount() != 0) {
            UpdateChain.of(AppComment.class)
                    .setRaw(AppComment::getDislikeCount, "dislike_count + 1")
                    .where(AppComment::getCommentId).eq(appCommentLikeRequest.getCommentId())
                    .update();
        }
        return true;
    }


    /**
     * 获取并清除用户未读的评论消息
     *
     * @param userId 用户ID
     * @return 未读的评论消息列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AppCommentVO> getAndClearUnreadAppComment(String userId) {
        // 1. 获取未读评论记录
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId)
                .eq("is_read", 0)
                // 添加这一行，排除自己评论自己的记录
                .ne("user_id", userId)
                .orderBy("create_time", false);
        List<AppComment> unreadComments = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadComments)) {
            return new ArrayList<>();
        }
        // 2. 批量更新为已读
        List<String> commentIds = unreadComments.stream()
                .map(AppComment::getCommentId)
                .collect(Collectors.toList());
        UpdateChain.of(AppComment.class)
                .setRaw(AppComment::getIsRead, 1)
                .where(AppComment::getCommentId).in(commentIds)
                .update();
        // 3. 构建返回数据
        return unreadComments.stream().map(appComment -> {
            AppCommentVO appCommentVO = new AppCommentVO();
            BeanUtils.copyProperties(appComment, appCommentVO);
            // 获取评论用户信息
            User commentUser = userService.getById(appComment.getUserId());
            if (commentUser != null) {
                AppCommentUserVO appCommentUserVO = new AppCommentUserVO();
                BeanUtils.copyProperties(commentUser, appCommentUserVO);
                appCommentVO.setAppCommentUserVO(appCommentUserVO);
            }
            // 获取评论应用信息
            App app = appService.getById(appComment.getAppId());
            if (app != null) {
                AppVO appVO = new AppVO();
                BeanUtils.copyProperties(app, appVO);
                appCommentVO.setAppVO(appVO);
            }
            // 递归获取子评论
            appCommentVO.setChildCommentList(getChildrenComments(appComment.getCommentId()));
            return appCommentVO;
        }).collect(Collectors.toList());
    }


    /**
     * 获取用户未读评论数量
     *
     * @param userId 用户ID
     * @return 未读评论数量
     */
    @Override
    public long getUnreadCommentsCount(String userId) {
        return this.count(new QueryWrapper()
                .eq("user_id", userId)
                .eq("is_read", 0)
                // 添加这一行，排除自己评论自己的记录
                .ne("user_id", userId));
    }

    /**
     * 清空用户未读评论
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllUnreadComments(String userId) {
        UpdateChain.of(AppComment.class)
                .setRaw(AppComment::getIsRead, 1)
                .where(AppComment::getUserId).eq(userId)
                .update();
    }

    /**
     * 获取评论历史
     *
     * @param appCommentQueryRequest 查询评论请求
     * @param userId 用户ID
     * @return 评论历史
     */
    @Override
    public Page<AppCommentVO> getCommentedHistory(AppCommentQueryRequest appCommentQueryRequest, String userId) {
        ThrowUtil.throwIf(appCommentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = appCommentQueryRequest.getCurrent();
        long size = appCommentQueryRequest.getPageSize();
        // 创建分页对象
        Page<AppComment> page = new Page<>(current, size);
        // 构建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        // 查询用户发出的评论
        queryWrapper.eq("user_id", userId)
                // 只查询未删除的评论
                .eq("is_deleted", 0)
                .orderBy("create_time", false);
        // 执行分页查询
        Page<AppComment> appCommentPage = this.page(page, queryWrapper);
        // 转换结果
        List<AppCommentVO> records = appCommentPage.getRecords().stream().map(appComment -> {
            AppCommentVO appCommentVO = new AppCommentVO();
            BeanUtils.copyProperties(appComment, appCommentVO);
            // 设置评论用户信息
            User commentUser = userService.getById(appComment.getUserId());
            if (commentUser != null) {
                AppCommentUserVO appCommentUserVO = new AppCommentUserVO();
                BeanUtils.copyProperties(commentUser, appCommentUserVO);
                appCommentVO.setAppCommentUserVO(appCommentUserVO);
            }
            // 获取评论应用信息
            App app = appService.getById(appComment.getAppId());
            if (app != null) {
                AppVO appVO = new AppVO();
                BeanUtils.copyProperties(app, appVO);
                appCommentVO.setAppVO(appVO);
            }
            return appCommentVO;
        }).collect(Collectors.toList());

        // 构建返回结果
        Page<AppCommentVO> appCommentVOPage = new Page<>(
                appCommentPage.getPageNumber(), appCommentPage.getPageSize(), appCommentPage.getTotalRow());
        appCommentVOPage.setRecords(records);
        return appCommentVOPage;
    }

    /**
     * 获取我的评论历史
     *
     * @param appCommentQueryRequest 查询评论请求
     * @param userId 用户ID
     * @return 我的评论历史
     */
    @Override
    public Page<AppCommentVO> getMyCommentHistory(AppCommentQueryRequest appCommentQueryRequest, String userId) {
        ThrowUtil.throwIf(appCommentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = appCommentQueryRequest.getCurrent();
        long size = appCommentQueryRequest.getPageSize();
        // 创建分页对象
        Page<AppComment> page = new Page<>(current, size);
        // 构建查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        // 查询用户发出的评论
        queryWrapper.eq("user_id", userId)
                // 只查询未删除的评论
                .eq("is_deleted", 0)
                // 排除自己评论自己的记录
                .ne("user_id", userId)
                .orderBy("create_time", false);
        // 执行分页查询
        Page<AppComment> appCommentPage = this.page(page, queryWrapper);
        // 转换结果
        List<AppCommentVO> records = appCommentPage.getRecords().stream().map(appComment -> {
            AppCommentVO appCommentVO = new AppCommentVO();
            BeanUtils.copyProperties(appComment, appCommentVO);
            // 设置评论用户信息
            User commentUser = userService.getById(appComment.getUserId());
            if (commentUser != null) {
                AppCommentUserVO appCommentUserVO = new AppCommentUserVO();
                BeanUtils.copyProperties(commentUser, appCommentUserVO);
                appCommentVO.setAppCommentUserVO(appCommentUserVO);
            }
            // 获取评论应用信息
            App app = appService.getById(appComment.getAppId());
            if (app != null) {
                AppVO appVO = new AppVO();
                BeanUtils.copyProperties(app, appVO);
                appCommentVO.setAppVO(appVO);
            }
            return appCommentVO;
        }).collect(Collectors.toList());

        // 构建返回结果
        Page<AppCommentVO> appCommentVOPage = new Page<>(
                appCommentPage.getPageNumber(), appCommentPage.getPageSize(), appCommentPage.getTotalRow());
        appCommentVOPage.setRecords(records);
        return appCommentVOPage;
    }

    /**
     * 更新评论数
     *
     * @param appId 应用ID
     */
    private void updateCommentCount(String appId) {
        App app = appService.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        }
        app.setCommentCount(app.getCommentCount() + 1);
        appService.updateById(app);
    }

    /**
     * 递归计算评论及其子评论的数量
     *
     * @param commentId 评论ID
     * @return 评论及其子评论的总数
     */
    private int countCommentsRecursively(String commentId) {
        // 获取所有未删除的子评论
        QueryWrapper queryWrapper = new QueryWrapper()
                .eq("parent_id", commentId)
                .eq("is_deleted", 0);
        List<AppComment> appComments = list(queryWrapper);
        // 递归计算子评论总数
        int totalCount = 1;
        if (CollectionUtil.isNotEmpty(appComments)) {
            for (AppComment appComment : appComments) {
                totalCount += countCommentsRecursively(appComment.getCommentId());
            }
        }
        return totalCount;
    }


    /**
     * 递归删除评论及其子评论
     *
     * @param commentId 评论ID
     * @return 删除结果
     */
    private boolean deleteCommentsRecursively(String commentId) {
        // 获取所有未删除的子评论
        QueryWrapper queryWrapper = new QueryWrapper()
                .eq("parent_id", commentId)
                .eq("is_deleted", 0);
        List<AppComment> appComments = list(queryWrapper);
        // 递归删除子评论
        if (CollectionUtil.isNotEmpty(appComments)) {
            for (AppComment appComment : appComments) {
                deleteCommentsRecursively(appComment.getCommentId());
            }
        }
        // 删除当前评论（逻辑删除）
        AppComment appComment = getById(commentId);
        appComment.setIsDeleted(1);
        updateById(appComment);
        return true;
    }

    /**
     * 递归查询子评论
     *
     * @param parentCommentId 顶级评论ID
     * @return 子评论列表
     */
    private List<AppCommentVO> getChildrenComments(String parentCommentId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("parent_id", parentCommentId);
        // 按照创建时间倒序排列
        queryWrapper.orderBy("create_time", false);
        // 使用 list 方法查询子评论
        List<AppComment> childrenComments = this.list(queryWrapper);
        if (childrenComments == null || childrenComments.isEmpty()) {
            return Collections.emptyList();
        }
        // 获取子评论的用户 id 列表
        List<String > childUserIds = childrenComments.stream()
                .map(AppComment::getUserId)
                .collect(Collectors.toList());
        // 批量查询子评论的用户信息
        List<User> childUsers = userService.listByIds(childUserIds);
        List<AppCommentUserVO> childCommentUserVOList = childUsers.stream().map(user -> {
            AppCommentUserVO appCommentUserVO = new AppCommentUserVO();
            BeanUtils.copyProperties(user, appCommentUserVO);
            return appCommentUserVO;
        }).toList();
        Map<String, AppCommentUserVO> childUserMap = childCommentUserVOList.stream()
                .collect(Collectors.toMap(AppCommentUserVO::getUserId, AppCommentUserVO -> AppCommentUserVO));
        return childrenComments.stream().map(comments -> {
            AppCommentVO appCommentVO = new AppCommentVO();
            BeanUtils.copyProperties(comments, appCommentVO);
            // 查找对应的子评论用户信息
            AppCommentUserVO commentUserVO = childUserMap.get(comments.getUserId());
            if (commentUserVO!= null) {
                appCommentVO.setAppCommentUserVO(commentUserVO);
            }
            // 递归调用，查询子评论的子评论
            appCommentVO.setChildCommentList(getChildrenComments(comments.getCommentId()));
            return appCommentVO;
        }).collect(Collectors.toList());
    }


}
