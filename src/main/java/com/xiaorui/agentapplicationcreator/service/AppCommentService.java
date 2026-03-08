package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentAddRequest;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentDeleteRequest;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentLikeRequest;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.AppComment;
import com.xiaorui.agentapplicationcreator.model.vo.AppCommentVO;

import java.util.List;

/**
 * 应用评论表 服务层。
 *
 * @author xiaorui
 */
public interface AppCommentService extends IService<AppComment> {

    /**
     * 添加评论
     *
     * @param appCommentAddRequest 添加评论请求
     * @return 添加评论结果
     */
    Boolean addAppComment(AppCommentAddRequest appCommentAddRequest);

    /**
     * 删除评论
     *
     * @param appCommentDeleteRequest 删除评论请求
     * @return 删除评论结果
     */
    Boolean deleteAppComment(AppCommentDeleteRequest appCommentDeleteRequest);

    /**
     * 查询评论
     *
     * @param appCommentQueryRequest 查询评论请求
     * @return 查询评论结果
     */
    Page<AppCommentVO> queryAppComment(AppCommentQueryRequest appCommentQueryRequest);

    /**
     * 点赞评论
     *
     * @param appCommentLikeRequest 点赞评论请求
     * @return 点赞评论结果
     */
    Boolean likeAppComment(AppCommentLikeRequest appCommentLikeRequest);

    /**
     * 获取并清除用户未读的评论消息
     *
     * @param userId 用户ID
     * @return 未读的评论消息列表
     */
    List<AppCommentVO> getAndClearUnreadAppComment(String userId);

    /**
     * 获取用户未读评论数量
     *
     * @param userId 用户ID
     * @return 未读评论数量
     */
    long getUnreadCommentsCount(String userId);

    /**
     * 清空用户未读评论
     *
     * @param userId 用户ID
     */
    void clearAllUnreadComments(String userId);

    /**
     * 获取评论历史
     *
     * @param appCommentQueryRequest 查询评论请求
     * @param userId 用户ID
     * @return 评论历史
     */
    Page<AppCommentVO> getCommentedHistory(AppCommentQueryRequest appCommentQueryRequest, String userId);

    /**
     * 获取我的评论历史
     *
     * @param appCommentQueryRequest 查询评论请求
     * @param userId 用户ID
     * @return 我的评论历史
     */
    Page<AppCommentVO> getMyCommentHistory(AppCommentQueryRequest appCommentQueryRequest, String userId);

}
