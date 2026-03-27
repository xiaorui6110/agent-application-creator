package com.xiaorui.agentapplicationcreator.controller;

import com.mybatisflex.core.query.QueryWrapper;
import com.xiaorui.agentapplicationcreator.constant.AppConstant;
import com.xiaorui.agentapplicationcreator.constant.LogicDeletedConstant;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import com.xiaorui.agentapplicationcreator.model.vo.AdminDashboardStatsVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.AgentTaskService;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.ChatHistoryService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理后台运营概览
 */
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private AgentTaskService agentTaskService;

    @GetMapping("/stats")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "获取后台运营概览统计", description = "获取后台运营概览统计")
    public ServerResponseEntity<AdminDashboardStatsVO> getDashboardStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        AdminDashboardStatsVO statsVO = new AdminDashboardStatsVO();
        statsVO.setTotalUserCount(userService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)));
        statsVO.setTodayRegisterCount(userService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)
                .ge("create_time", todayStart)));

        statsVO.setTotalAppCount(appService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)));
        statsVO.setTodayAppCount(appService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)
                .ge("create_time", todayStart)));
        statsVO.setDeployedAppCount(appService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)
                .isNotNull("deploy_url")));
        statsVO.setFeaturedAppCount(appService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)
                .eq("app_priority", AppConstant.GOOD_APP_PRIORITY)));

        statsVO.setTotalChatCount(chatHistoryService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)));
        statsVO.setTodayChatCount(chatHistoryService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)
                .ge("create_time", todayStart)));

        statsVO.setTotalTaskCount(agentTaskService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)));
        statsVO.setTodayTaskCount(agentTaskService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)
                .ge("create_time", todayStart)));
        statsVO.setRunningTaskCount(agentTaskService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)
                .eq("task_status", "RUNNING")));
        statsVO.setWaitingTaskCount(agentTaskService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)
                .in("task_status", "WAITING", "RETRY_WAITING", "INIT", "QUEUED")));
        statsVO.setSucceededTaskCount(agentTaskService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)
                .eq("task_status", "SUCCEEDED")));
        statsVO.setFailedTaskCount(agentTaskService.count(QueryWrapper.create()
                .eq("is_deleted", LogicDeletedConstant.LOGIC_DELETED_NO)
                .eq("task_status", "FAILED")));

        return ServerResponseEntity.success(statsVO);
    }
}
