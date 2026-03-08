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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 点赞记录表 控制层。
 *
 * @author xiaorui
 */
@Slf4j
@RestController
@RequestMapping("/likeRecord")
public class LikeRecordController {

    @Resource
    private LikeRecordService likeRecordService;

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/do")
    @Operation(summary = "点赞/取消点赞" , description = "点赞/取消点赞")
    @Parameter(name = "likeDoRequest", description = "点赞/取消点赞请求")
    public ServerResponseEntity<Boolean> doLike(@RequestBody LikeDoRequest likeDoRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        try {
            likeRecordService.doLike(likeDoRequest, userId);
            return ServerResponseEntity.success(true);
        } catch (Exception e) {
            log.error("Error in doLike controller: ", e);
            return ServerResponseEntity.success(false);
        }
    }

    /**
     * 获取点赞状态
     */
    @GetMapping("/status/{targetId}")
    @Operation(summary = "获取点赞状态" , description = "获取点赞状态")
    @Parameter(name = "targetId", description = "目标ID")
    public ServerResponseEntity<Boolean> getLikeStatus(@PathVariable("targetId") String targetId) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(likeRecordService.isContentLiked(targetId, userId));
    }

    /**
     * 获取未读点赞记录
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读点赞记录" , description = "获取未读点赞记录")
    public ServerResponseEntity<List<LikeRecordVO>> getUnreadLikes() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(likeRecordService.getAndClearUnreadLikes(userId));
    }

    /**
     * 获取点赞历史
     */
    @PostMapping("/history")
    @Operation(summary = "获取点赞历史" , description = "获取点赞历史")
    @Parameter(name = "likeQueryRequest", description = "获取点赞历史请求")
    public ServerResponseEntity<Page<LikeRecordVO>> getLikeHistory(@RequestBody LikeQueryRequest likeQueryRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(likeRecordService.getMyLikeHistory(likeQueryRequest, userId));
    }

    /**
     * 获取我的点赞历史
     */
    @PostMapping("/my/history")
    @Operation(summary = "获取我的点赞历史" , description = "获取我的点赞历史")
    @Parameter(name = "likeQueryRequest", description = "获取我的点赞历史请求")
    public ServerResponseEntity<Page<LikeRecordVO>> getMyLikeHistory(@RequestBody LikeQueryRequest likeQueryRequest) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(likeRecordService.getUserLikeHistory(likeQueryRequest, userId));
    }

    /**
     * 获取未读点赞记录数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读点赞记录数量" , description = "获取未读点赞记录数量")
    public ServerResponseEntity<Long> getUnreadLikesCount() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(likeRecordService.getUnreadLikesCount(userId));
    }

}
