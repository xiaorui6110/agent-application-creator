package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理后台运营概览统计
 * @author xiaorui
 */
@Data
public class AdminDashboardStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1528574123343113408L;

    @Schema(description = "用户总数")
    private Long totalUserCount;

    @Schema(description = "今日新增用户数")
    private Long todayRegisterCount;

    @Schema(description = "应用总数")
    private Long totalAppCount;

    @Schema(description = "今日新增应用数")
    private Long todayAppCount;

    @Schema(description = "已部署应用数")
    private Long deployedAppCount;

    @Schema(description = "精选应用数")
    private Long featuredAppCount;

    @Schema(description = "总对话数")
    private Long totalChatCount;

    @Schema(description = "今日新增对话数")
    private Long todayChatCount;

    @Schema(description = "总任务数")
    private Long totalTaskCount;

    @Schema(description = "今日新增任务数")
    private Long todayTaskCount;

    @Schema(description = "运行中任务数")
    private Long runningTaskCount;

    @Schema(description = "等待中任务数")
    private Long waitingTaskCount;

    @Schema(description = "成功任务数")
    private Long succeededTaskCount;

    @Schema(description = "失败任务数")
    private Long failedTaskCount;

}
