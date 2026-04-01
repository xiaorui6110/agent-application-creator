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
 * @author xiaorui
 */
public interface AppCommentService extends IService<AppComment> {

    Boolean addAppComment(AppCommentAddRequest appCommentAddRequest);

    Boolean deleteAppComment(AppCommentDeleteRequest appCommentDeleteRequest);

    Page<AppCommentVO> queryAppComment(AppCommentQueryRequest appCommentQueryRequest);

    Boolean likeAppComment(AppCommentLikeRequest appCommentLikeRequest);

    List<AppCommentVO> listUnreadAppComments(String userId, int limit);

    void markCommentsAsRead(String userId, List<String> commentIds);

    List<AppCommentVO> getAndClearUnreadAppComment(String userId);

    long getUnreadCommentsCount(String userId);

    void clearAllUnreadComments(String userId);

    Page<AppCommentVO> getCommentedHistory(AppCommentQueryRequest appCommentQueryRequest, String userId);

    Page<AppCommentVO> getMyCommentHistory(AppCommentQueryRequest appCommentQueryRequest, String userId);
}
