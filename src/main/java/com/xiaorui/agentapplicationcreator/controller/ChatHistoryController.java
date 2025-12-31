package com.xiaorui.agentapplicationcreator.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xiaorui.agentapplicationcreator.constants.UserConstant;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import com.xiaorui.agentapplicationcreator.model.dto.chathistory.ChatHistoryQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.ChatHistory;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.ChatHistoryService;
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
    public ServerResponseEntity<Page<ChatHistory>> listAppChatHistory(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtil.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String appId = chatHistoryQueryRequest.getAppId();
        int pageSize = chatHistoryQueryRequest.getPageSize();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        return ServerResponseEntity.success(chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime));
    }

    /**
     *
     * 【管理员】分页查询所有对话历史（游标查询）
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public ServerResponseEntity<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtil.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        int current = chatHistoryQueryRequest.getCurrent();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        return ServerResponseEntity.success(chatHistoryService.page(Page.of(current, pageSize), queryWrapper));
    }


}
