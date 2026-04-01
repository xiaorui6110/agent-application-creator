package com.xiaorui.agentapplicationcreator.controller;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareDoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareQueryRequest;
import com.xiaorui.agentapplicationcreator.model.vo.SharePreviewVO;
import com.xiaorui.agentapplicationcreator.model.vo.ShareRecordVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.ShareRecordService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xiaorui
 */
@RestController
@RequestMapping("/shareRecord")
public class ShareRecordController {

    @Resource
    private ShareRecordService shareRecordService;

    @PostMapping("/do")
    @Operation(summary = "share or cancel share", description = "share or cancel share")
    @Parameter(name = "shareDoRequest", description = "share request")
    public ServerResponseEntity<Boolean> doShare(@RequestBody ShareDoRequest shareDoRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(shareRecordService.doShare(shareDoRequest, userId));
    }

    @GetMapping("/preview/{targetId}")
    @Operation(summary = "get share preview", description = "get share preview")
    @Parameter(name = "targetId", description = "target id")
    public ServerResponseEntity<SharePreviewVO> getSharePreview(@PathVariable("targetId") String targetId) {
        ThrowUtil.throwIf(StrUtil.isBlank(targetId), ErrorCode.PARAMS_ERROR, "target id is blank");
        return ServerResponseEntity.success(shareRecordService.getSharePreview(targetId));
    }

    @GetMapping("/status/{targetId}")
    @Operation(summary = "get share status", description = "get share status")
    @Parameter(name = "targetId", description = "target id")
    public ServerResponseEntity<Boolean> getShareStatus(@PathVariable("targetId") String targetId) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(shareRecordService.isContentShared(targetId, userId));
    }

    @GetMapping("/unread")
    @Operation(summary = "get unread shares", description = "get unread shares")
    public ServerResponseEntity<List<ShareRecordVO>> getUnreadShares() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(shareRecordService.getAndClearUnreadShares(userId));
    }

    @PostMapping("/history")
    @Operation(summary = "get received share history", description = "get received share history")
    @Parameter(name = "shareQueryRequest", description = "share history request")
    public ServerResponseEntity<Page<ShareRecordVO>> getUserShareHistory(@RequestBody ShareQueryRequest shareQueryRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(shareRecordService.getUserShareHistory(shareQueryRequest, userId));
    }

    @PostMapping("/my/history")
    @Operation(summary = "get my share history", description = "get my share history")
    @Parameter(name = "shareQueryRequest", description = "my share history request")
    public ServerResponseEntity<Page<ShareRecordVO>> getMyShareHistory(@RequestBody ShareQueryRequest shareQueryRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(shareRecordService.getMyShareHistory(shareQueryRequest, userId));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "get unread share count", description = "get unread share count")
    public ServerResponseEntity<Long> getUnreadSharesCount() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(shareRecordService.getUnreadSharesCount(userId));
    }
}
