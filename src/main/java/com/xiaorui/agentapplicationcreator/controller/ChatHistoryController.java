package com.xiaorui.agentapplicationcreator.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xiaorui.agentapplicationcreator.common.DeleteRequest;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import com.xiaorui.agentapplicationcreator.model.dto.chathistory.ChatHistoryQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.ChatHistory;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.ChatHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 对话历史表 控制层。
 *
 * @author xiaorui
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     */
    @GetMapping("/app/{appId}")
    @Operation(summary = "分页查询某个应用的对话历史" , description = "分页查询某个应用的对话历史")
    @Parameter(name = "chatHistoryQueryRequest", description = "对话历史查询请求参数")
    public ServerResponseEntity<Page<ChatHistory>> listAppChatHistory(@PathVariable String appId,
                                                                      @RequestParam(defaultValue = "10") int pageSize,
                                                                      @RequestParam(required = false) LocalDateTime lastCreateTime) {
        return ServerResponseEntity.success(chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime));
    }

    /**
     *
     * 【管理员】分页查询所有对话历史（游标查询）
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员分页查询所有对话历史" , description = "管理员分页查询所有对话历史")
    @Parameter(name = "chatHistoryQueryRequest", description = "对话历史查询请求参数")
    public ServerResponseEntity<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtil.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        int current = chatHistoryQueryRequest.getCurrent();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        return ServerResponseEntity.success(chatHistoryService.page(Page.of(current, pageSize), queryWrapper));
    }


    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员删除对话历史" , description = "管理员删除对话历史")
    public ServerResponseEntity<Boolean> deleteChatHistoryByAdmin(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtil.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String id = deleteRequest.getId();
        ThrowUtil.throwIf(id == null || id.isBlank(), ErrorCode.PARAMS_ERROR, "id不能为空");
        ChatHistory oldChatHistory = chatHistoryService.getById(id);
        ThrowUtil.throwIf(oldChatHistory == null, ErrorCode.NOT_FOUND_ERROR, "对话历史不存在");
        return ServerResponseEntity.success(chatHistoryService.removeById(id));
    }
}
