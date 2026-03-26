package com.xiaorui.agentapplicationcreator.controller;

import cn.hutool.core.util.StrUtil;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.model.vo.CommunityNotificationVO;
import com.xiaorui.agentapplicationcreator.model.vo.CommunityUnreadSummaryVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.CommunityService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Resource
    private CommunityService communityService;

    @GetMapping("/unread/summary")
    @Operation(summary = "get community unread summary", description = "get community unread summary")
    public ServerResponseEntity<CommunityUnreadSummaryVO> getUnreadSummary() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(communityService.getUnreadSummary(userId));
    }

    @GetMapping("/unread/feed")
    @Operation(summary = "get unread community notifications", description = "get unread community notifications")
    @Parameter(name = "limit", description = "max unread notification size")
    public ServerResponseEntity<List<CommunityNotificationVO>> getUnreadFeed(
            @RequestParam(value = "limit", required = false) Integer limit) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        return ServerResponseEntity.success(communityService.getAndClearUnreadNotifications(userId, limit));
    }

    @PostMapping("/unread/clear")
    @Operation(summary = "clear all unread community notifications", description = "clear all unread community notifications")
    public ServerResponseEntity<Boolean> clearUnreadFeed() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(StrUtil.isBlank(userId), ErrorCode.NOT_FOUND_ERROR, "user not found");
        communityService.clearAllUnreadNotifications(userId);
        return ServerResponseEntity.success(true);
    }
}
