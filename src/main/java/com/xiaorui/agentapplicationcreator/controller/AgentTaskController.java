package com.xiaorui.agentapplicationcreator.controller;

import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.model.entity.AgentTask;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.AgentTaskService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * agent执行任务表 控制层。
 *
 * @author xiaorui
 */
@RestController
@RequestMapping("/agentTask")
public class AgentTaskController {

    @Resource
    private AgentTaskService agentTaskService;

    /**
     * 轮询获取任务结果（前端轮询）
     */
    @GetMapping("/getTask")
    public ServerResponseEntity<SystemOutput> getTaskState(@RequestParam String taskId) {
        ThrowUtil.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        return ServerResponseEntity.success(agentTaskService.getTask(taskId));
    }

    /**
     * 手动重试任务
     */
    @PostMapping("/retry/{taskId}")
    public ServerResponseEntity<Void> retryTask(@PathVariable String taskId) {
        ThrowUtil.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        agentTaskService.manualRetryTask(taskId);
        return ServerResponseEntity.success();
    }

    /**
     * 查询所有agent执行任务表。
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    public List<AgentTask> list() {
        return agentTaskService.list();
    }

    /**
     * 根据主键获取agent执行任务表。
     *
     * @param id agent执行任务表主键
     * @return agent执行任务表详情
     */
    @GetMapping("/getInfo/{id}")
    public AgentTask getInfo(@PathVariable String id) {
        return agentTaskService.getById(id);
    }

    /**
     * 分页查询agent执行任务表。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    public Page<AgentTask> page(Page<AgentTask> page) {
        return agentTaskService.page(page);
    }

}
