package com.xiaorui.agentapplicationcreator.controller;

import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import com.xiaorui.agentapplicationcreator.manager.stream.AgentTaskStreamManager;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.AgentTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/agentTask")
public class AgentTaskController {

    @Resource
    private AgentTaskService agentTaskService;

    @Resource
    private AgentTaskStreamManager agentTaskStreamManager;

    @GetMapping("/getTask")
    @Operation(summary = "轮询获取任务结果", description = "前端轮询")
    @Parameter(name = "taskId", description = "任务ID")
    public ServerResponseEntity<SystemOutput> getTaskState(@RequestParam String taskId) {
        ThrowUtil.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        return ServerResponseEntity.success(agentTaskService.getTask(taskId));
    }

    @GetMapping(value = "/stream/{taskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式获取任务过程事件", description = "SSE task stream")
    @Parameter(name = "taskId", description = "任务ID")
    public SseEmitter streamTask(@PathVariable String taskId) {
        ThrowUtil.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        SseEmitter emitter = agentTaskStreamManager.subscribe(taskId);
        SystemOutput snapshot = agentTaskService.getTask(taskId);
        agentTaskStreamManager.publishConnected(taskId, snapshot);
        if ("SUCCEEDED".equals(snapshot.getTaskStatus())) {
            agentTaskStreamManager.publishDone(taskId, snapshot);
        } else if ("FAILED".equals(snapshot.getTaskStatus())) {
            agentTaskStreamManager.publishFailed(taskId, snapshot);
        } else {
            agentTaskStreamManager.publishStatus(taskId, snapshot);
        }
        return emitter;
    }

    @PostMapping("/retry/{taskId}")
    @Operation(summary = "手动重试任务", description = "手动重试任务")
    @Parameter(name = "taskId", description = "任务ID")
    public ServerResponseEntity<Void> retryTask(@PathVariable String taskId) {
        ThrowUtil.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        agentTaskService.manualRetryTask(taskId);
        return ServerResponseEntity.success();
    }

    @GetMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "查询所有 agent 执行任务表", description = "查询所有 agent 执行任务表")
    @Parameter(name = "taskId", description = "任务ID")
    public List<AgentTask> list() {
        return agentTaskService.list();
    }

    @GetMapping("/getInfo/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "根据主键获取 agent 执行任务表", description = "根据主键获取 agent 执行任务表")
    @Parameter(name = "taskId", description = "任务ID")
    public AgentTask getInfo(@PathVariable String id) {
        return agentTaskService.getById(id);
    }

    @GetMapping("/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "分页查询 agent 执行任务表", description = "分页查询 agent 执行任务表")
    @Parameter(name = "page", description = "AgentTask 分页对象")
    public Page<AgentTask> page(Page<AgentTask> page) {
        return agentTaskService.page(page);
    }
}
