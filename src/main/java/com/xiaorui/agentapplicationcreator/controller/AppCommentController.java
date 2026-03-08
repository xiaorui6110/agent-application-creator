package com.xiaorui.agentapplicationcreator.controller;

import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.constant.CrawlerConstant;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentAddRequest;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentDeleteRequest;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentLikeRequest;
import com.xiaorui.agentapplicationcreator.model.dto.appcomment.AppCommentQueryRequest;
import com.xiaorui.agentapplicationcreator.model.vo.AppCommentVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.AppCommentService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用评论表 控制层。
 *
 * @author xiaorui
 */
@RestController
@RequestMapping("/appComment")
public class AppCommentController {

    @Resource
    private AppCommentService appCommentService;

    @Resource
    private UserService userService;

    /**
     * 添加评论
     */
    @PostMapping("/add")
    @Operation(summary = "添加评论" , description = "添加评论")
    @Parameter(name = "appCommentAddRequest", description = "添加评论请求")
    public ServerResponseEntity<Boolean> addComment(@RequestBody AppCommentAddRequest appCommentAddRequest) {
        ThrowUtil.throwIf(appCommentAddRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        return ServerResponseEntity.success(appCommentService.addAppComment(appCommentAddRequest));
    }

    /**
     * 删除评论
     */
    @PostMapping("/delete")
    @Operation(summary = "删除评论" , description = "删除评论")
    @Parameter(name = "appCommentDeleteRequest", description = "删除评论请求")
    public ServerResponseEntity<Boolean> deleteComment(@RequestBody AppCommentDeleteRequest appCommentDeleteRequest) {
        ThrowUtil.throwIf(appCommentDeleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        return ServerResponseEntity.success(appCommentService.deleteAppComment(appCommentDeleteRequest));
    }

    /**
     * 查询评论
     */
    @PostMapping("/query")
    @Operation(summary = "查询评论" , description = "查询评论")
    @Parameter(name = "appCommentQueryRequest", description = "查询评论请求")
    public ServerResponseEntity<Page<AppCommentVO>> queryComment(@RequestBody AppCommentQueryRequest appCommentQueryRequest) {
        ThrowUtil.throwIf(appCommentQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String userId = SecurityUtil.getUserInfo().getUserId();
        if (userId != null) {
            String userRole = userService.getById(userId).getUserRole();
            ThrowUtil.throwIf(userRole.equals(CrawlerConstant.BAN_ROLE), ErrorCode.NOT_AUTH_ERROR, "封禁用户禁止获取数据,请联系管理员");
        }
        long size = appCommentQueryRequest.getPageSize();
        ThrowUtil.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页最多查询20条数据");
        return ServerResponseEntity.success(appCommentService.queryAppComment(appCommentQueryRequest));
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like")
    @Operation(summary = "点赞评论" , description = "点赞评论")
    @Parameter(name = "appCommentLikeRequest", description = "点赞评论请求")
    public ServerResponseEntity<Boolean> likeComment(@RequestBody AppCommentLikeRequest appCommentLikeRequest) {
        ThrowUtil.throwIf(appCommentLikeRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        return ServerResponseEntity.success(appCommentService.likeAppComment(appCommentLikeRequest));
    }

    /**
     * 获取未读评论
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读评论" , description = "获取未读评论")
    public ServerResponseEntity<List<AppCommentVO>> unreadComment() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(userId == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(appCommentService.getAndClearUnreadAppComment(userId));
    }

    /**
     * 获取未读评论数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读评论数量" , description = "获取未读评论数量")
    public ServerResponseEntity<Long> unreadCommentCount() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(userId == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ServerResponseEntity.success(appCommentService.getUnreadCommentsCount(userId));
    }

    /**
     * 获取评论历史
     */
    @PostMapping("/commented/history")
    @Operation(summary = "获取评论历史" , description = "获取评论历史")
    @Parameter(name = "appCommentQueryRequest", description = "获取评论历史请求")
    public ServerResponseEntity<Page<AppCommentVO>> commentedHistory(@RequestBody AppCommentQueryRequest appCommentQueryRequest) {
        ThrowUtil.throwIf(appCommentQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(userId == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        long size = appCommentQueryRequest.getPageSize();
        ThrowUtil.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页最多查询20条数据");
        return ServerResponseEntity.success(appCommentService.getCommentedHistory(appCommentQueryRequest, userId));
    }

    /**
     * 获取我的评论历史
     */
    @PostMapping("/my/history")
    @Operation(summary = "获取我的评论历史" , description = "获取我的评论历史")
    @Parameter(name = "appCommentQueryRequest", description = "获取我的评论历史请求")
    public ServerResponseEntity<Page<AppCommentVO>> myHistory(@RequestBody AppCommentQueryRequest appCommentQueryRequest) {
        ThrowUtil.throwIf(appCommentQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String userId = SecurityUtil.getUserInfo().getUserId();
        ThrowUtil.throwIf(userId == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        long size = appCommentQueryRequest.getPageSize();
        ThrowUtil.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页最多查询20条数据");
        return ServerResponseEntity.success(appCommentService.getMyCommentHistory(appCommentQueryRequest, userId));
    }

}
