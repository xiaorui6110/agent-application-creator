package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.dto.likerecord.LikeDoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.likerecord.LikeQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.LikeRecord;
import com.xiaorui.agentapplicationcreator.model.vo.LikeRecordVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 点赞记录表 服务层。
 *
 * @author xiaorui
 */
public interface LikeRecordService extends IService<LikeRecord> {

    /**
     * 通用点赞/取消点赞
     *
     * @param likeDoRequest 点赞请求
     * @param userId 用户ID
     * @return 点赞结果
     */
    CompletableFuture<Boolean> doLike(LikeDoRequest likeDoRequest, String userId);

    /**
     * 获取并清除用户未读的点赞消息
     *
     * @param userId 用户ID
     * @return 点赞消息列表
     */
    List<LikeRecordVO> getAndClearUnreadLikes(String userId);

    /**
     * 获取用户的点赞历史（分页）
     *
     * @param likeQueryRequest 点赞查询请求
     * @param userId 用户ID
     * @return 点赞历史列表
     */
    Page<LikeRecordVO> getUserLikeHistory(LikeQueryRequest likeQueryRequest, String userId);

    /**
     * 检查内容是否已被用户点赞
     *
     * @param targetId 目标ID
     * @param userId 用户ID
     * @return 是否已被点赞
     */
    boolean isContentLiked(String targetId, String userId);

    /**
     * 获取用户未读点赞数
     *
     * @param userId 用户ID
     * @return 未读点赞数
     */
    long getUnreadLikesCount(String userId);

    /**
     * 清除用户所有未读点赞状态
     *
     * @param userId 用户ID
     */
    void clearAllUnreadLikes(String userId);

    /**
     * 获取用户自己的点赞历史（分页）
     *
     * @param likeQueryRequest 点赞查询请求
     * @param userId 用户ID
     * @return 点赞历史列表
     */
    Page<LikeRecordVO> getMyLikeHistory(LikeQueryRequest likeQueryRequest, String userId);

}
