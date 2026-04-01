package com.xiaorui.agentapplicationcreator.controller;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.model.dto.likerecord.LikeDoRequest;
import com.xiaorui.agentapplicationcreator.model.dto.likerecord.LikeQueryRequest;
import com.xiaorui.agentapplicationcreator.model.vo.LikeRecordVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.LikeRecordService;
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
@RequestMapping("/likeRecord")
public class LikeRecordController {

    @Resource
    private LikeRecordService likeRecordService;

    @PostMapping("/do")
    @Operation(summary = "like or cancel like", description = "like or cancel like")
    @Parameter(name = "likeDoRequest", description = "like request")
    public ServerResponseEntity<Boolean> doLike(@RequestBody LikeDoRequest likeDoRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(likeRecordService.doLike(likeDoRequest, userId));
    }

    @GetMapping("/status/{targetId}")
    @Operation(summary = "get like status", description = "get like status")
    @Parameter(name = "targetId", description = "target id")
    public ServerResponseEntity<Boolean> getLikeStatus(@PathVariable("targetId") String targetId) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(likeRecordService.isContentLiked(targetId, userId));
    }

    @GetMapping("/unread")
    @Operation(summary = "get unread likes", description = "get unread likes")
    public ServerResponseEntity<List<LikeRecordVO>> getUnreadLikes() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(likeRecordService.getAndClearUnreadLikes(userId));
    }

    @PostMapping("/history")
    @Operation(summary = "get received like history", description = "get received like history")
    @Parameter(name = "likeQueryRequest", description = "like history request")
    public ServerResponseEntity<Page<LikeRecordVO>> getLikeHistory(@RequestBody LikeQueryRequest likeQueryRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(likeRecordService.getUserLikeHistory(likeQueryRequest, userId));
    }

    @PostMapping("/my/history")
    @Operation(summary = "get my like history", description = "get my like history")
    @Parameter(name = "likeQueryRequest", description = "my like history request")
    public ServerResponseEntity<Page<LikeRecordVO>> getMyLikeHistory(@RequestBody LikeQueryRequest likeQueryRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(likeRecordService.getMyLikeHistory(likeQueryRequest, userId));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "get unread like count", description = "get unread like count")
    public ServerResponseEntity<Long> getUnreadLikesCount() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(likeRecordService.getUnreadLikesCount(userId));
    }
}
