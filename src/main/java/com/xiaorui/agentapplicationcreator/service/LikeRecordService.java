package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.dto.likerecord.LikeDoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.likerecord.LikeQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.LikeRecord;
import com.xiaorui.agentapplicationcreator.model.vo.LikeRecordVO;

import java.util.List;

/**
 * @author xiaorui
 */
public interface LikeRecordService extends IService<LikeRecord> {

    boolean doLike(LikeDoRequest likeDoRequest, String userId);

    List<LikeRecordVO> listUnreadLikes(String userId, int limit);

    void markLikesAsRead(String userId, List<String> likeIds);

    List<LikeRecordVO> getAndClearUnreadLikes(String userId);

    Page<LikeRecordVO> getUserLikeHistory(LikeQueryRequest likeQueryRequest, String userId);

    boolean isContentLiked(String targetId, String userId);

    long getUnreadLikesCount(String userId);

    void clearAllUnreadLikes(String userId);

    Page<LikeRecordVO> getMyLikeHistory(LikeQueryRequest likeQueryRequest, String userId);
}
