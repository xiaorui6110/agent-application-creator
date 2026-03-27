package com.xiaorui.agentapplicationcreator.model.vo;

import lombok.Data;

/**
 * 管理后台运营概览统计
 */
@Data
public class AdminDashboardStatsVO {

    private Long totalUserCount;

    private Long todayRegisterCount;

    private Long totalAppCount;

    private Long todayAppCount;

    private Long deployedAppCount;

    private Long featuredAppCount;

    private Long totalChatCount;

    private Long todayChatCount;

    private Long totalTaskCount;

    private Long todayTaskCount;

    private Long runningTaskCount;

    private Long waitingTaskCount;

    private Long succeededTaskCount;

    private Long failedTaskCount;
}
