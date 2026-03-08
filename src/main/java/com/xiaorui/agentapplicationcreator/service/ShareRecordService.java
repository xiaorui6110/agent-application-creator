package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareDoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.ShareRecord;
import com.xiaorui.agentapplicationcreator.model.vo.ShareRecordVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 分享记录表 服务层。
 *
 * @author xiaorui
 */
public interface ShareRecordService extends IService<ShareRecord> {

    /**
     * 分享
     *
     * @param shareDoRequest 分享请求
     * @param userId 用户ID
     * @return 分享结果
     */
    CompletableFuture<Boolean> doShare(ShareDoRequest shareDoRequest, String userId);

    /**
     * 获取并清除用户未读的分享消息
     *
     * @param userId 用户ID
     * @return 分享记录
     */
    List<ShareRecordVO> getAndClearUnreadShares(String userId);

    /**
     * 获取用户分享历史（分页）
     *
     * @param shareQueryRequest 查询分享请求
     * @param userId 用户ID
     * @return 分享记录
     */
    Page<ShareRecordVO> getUserShareHistory(ShareQueryRequest shareQueryRequest, String userId);

    /**
     * 判断内容是否被分享过
     *
     * @param targetId 目标ID
     * @param userId 用户ID
     * @return 是否被分享过
     */
    boolean isContentShared(String targetId, String userId);

    /**
     * 获取用户未读分享数量
     *
     * @param userId 用户ID
     * @return 未读分享数量
     */
    long getUnreadSharesCount(String userId);

    /**
     * 清除用户所有未读分享状态
     *
     * @param userId 用户ID
     */
    void clearAllUnreadShares(String userId);

    /**
     * 获取用户自己的分享历史（分页）
     *
     * @param shareQueryRequest 查询分享请求
     * @param userId 用户ID
     * @return 分享记录
     */
    Page<ShareRecordVO> getMyShareHistory(ShareQueryRequest shareQueryRequest, String userId);

}
