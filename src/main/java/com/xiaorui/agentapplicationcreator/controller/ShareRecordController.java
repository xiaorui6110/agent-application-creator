package com.xiaorui.agentapplicationcreator.controller;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareDoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.sharerecord.ShareQueryRequest;
import com.xiaorui.agentapplicationcreator.model.vo.ShareRecordVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.ShareRecordService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分享记录表 控制层。
 *
 * @author xiaorui
 */
@Slf4j
@RestController
@RequestMapping("/shareRecord")
public class ShareRecordController {

    @Resource
    private ShareRecordService shareRecordService;

    /**
     * 分享/取消分享
     */
    @PostMapping("/do")
    @Operation(summary = "分享/取消分享" , description = "分享/取消分享")
    @Parameter(name = "shareDoRequest", description = "分享/取消分享请求")
    public ServerResponseEntity<Boolean> doShare(@RequestBody ShareDoRequest shareDoRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        try {
            shareRecordService.doShare(shareDoRequest, userId);
            return ServerResponseEntity.success(true);
        } catch (Exception e) {
            log.error("Error in doShare controller: ", e);
            return ServerResponseEntity.success(false);
        }
    }

    /**
     * 获取分享状态
     */
    @GetMapping("/status/{targetId}")
    @Operation(summary = "获取分享状态" , description = "获取分享状态")
    @Parameter(name = "targetId", description = "目标ID")
    public ServerResponseEntity<Boolean> getShareStatus(@PathVariable("targetId") String targetId) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(shareRecordService.isContentShared(targetId, userId));
    }

    /**
     * 获取未读分享记录
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读分享记录" , description = "获取未读分享记录")
    public ServerResponseEntity<List<ShareRecordVO>> getUnreadShares() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(shareRecordService.getAndClearUnreadShares(userId));
    }

    /**
     * 获取分享历史
     */
    @PostMapping("/history")
    @Operation(summary = "获取分享历史" , description = "获取分享历史")
    @Parameter(name = "shareQueryRequest", description = "分享历史查询请求")
    public ServerResponseEntity<Page<ShareRecordVO>> getUserShareHistory(@RequestBody ShareQueryRequest shareQueryRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(shareRecordService.getUserShareHistory(shareQueryRequest, userId));
    }

    /**
     * 获取我的分享历史
     */
    @PostMapping("/my/history")
    @Operation(summary = "获取我的分享历史" , description = "获取我的分享历史")
    @Parameter(name = "shareQueryRequest", description = "分享历史查询请求")
    public ServerResponseEntity<Page<ShareRecordVO>> getMyShareHistory(@RequestBody ShareQueryRequest shareQueryRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(shareRecordService.getMyShareHistory(shareQueryRequest, userId));
    }

    /**
     * 获取未读分享记录数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读分享记录数量" , description = "获取未读分享记录数量")
    public ServerResponseEntity<Long> getUnreadSharesCount() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(shareRecordService.getUnreadSharesCount(userId));
    }
}
