package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareDoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.ShareRecord;
import com.xiaorui.agentapplicationcreator.model.vo.SharePreviewVO;
import com.xiaorui.agentapplicationcreator.model.vo.ShareRecordVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 分享记录表 服务层
 *
 * @author xiaorui
 */
public interface ShareRecordService extends IService<ShareRecord> {

    CompletableFuture<Boolean> doShare(ShareDoRequest shareDoRequest, String userId);

    List<ShareRecordVO> getAndClearUnreadShares(String userId);

    Page<ShareRecordVO> getUserShareHistory(ShareQueryRequest shareQueryRequest, String userId);

    boolean isContentShared(String targetId, String userId);

    long getUnreadSharesCount(String userId);

    void clearAllUnreadShares(String userId);

    Page<ShareRecordVO> getMyShareHistory(ShareQueryRequest shareQueryRequest, String userId);

    SharePreviewVO getSharePreview(String targetId);
}
