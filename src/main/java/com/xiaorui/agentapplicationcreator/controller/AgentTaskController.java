package com.xiaorui.agentapplicationcreator.controller;

import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.AgentTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * agent执行任务表 控制层。
 *
 * @author xiaorui
 */
//@Tag(name = "agent执行任务接口")
@RestController
@RequestMapping("/agentTask")
public class AgentTaskController {

    @Resource
    private AgentTaskService agentTaskService;

    /**
     * 轮询获取任务结果（前端轮询）
     */
    @GetMapping("/getTask")
    @Operation(summary = "轮询获取任务结果" , description = "前端轮询")
    @Parameter(name = "taskId", description = "任务ID")
    public ServerResponseEntity<SystemOutput> getTaskState(@RequestParam String taskId) {
        ThrowUtil.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        return ServerResponseEntity.success(agentTaskService.getTask(taskId));
    }

    /**
     * 手动重试任务
     */
    @PostMapping("/retry/{taskId}")
    @Operation(summary = "手动重试任务" , description = "手动重试任务")
    @Parameter(name = "taskId", description = "任务ID")
    public ServerResponseEntity<Void> retryTask(@PathVariable String taskId) {
        ThrowUtil.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        agentTaskService.manualRetryTask(taskId);
        return ServerResponseEntity.success();
    }

    /**
     * 【管理员】查询所有agent执行任务表。
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "查询所有agent执行任务表" , description = "查询所有agent执行任务表")
    @Parameter(name = "taskId", description = "任务ID")
    public List<AgentTask> list() {
        return agentTaskService.list();
    }

    /**
     * 【管理员】根据主键获取agent执行任务表。
     *
     * @param id agent执行任务表主键
     * @return agent执行任务表详情
     */
    @GetMapping("/getInfo/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "根据主键获取agent执行任务表" , description = "根据主键获取agent执行任务表")
    @Parameter(name = "taskId", description = "任务ID")
    public AgentTask getInfo(@PathVariable String id) {
        return agentTaskService.getById(id);
    }

    /**
     * 【管理员】分页查询agent执行任务表。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "分页查询agent执行任务表" , description = "分页查询agent执行任务表")
    @Parameter(name = "page", description = "AgentTask分页对象")
    public Page<AgentTask> page(Page<AgentTask> page) {
        return agentTaskService.page(page);
    }

}
