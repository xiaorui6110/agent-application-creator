package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author xiaorui
 */
@Data
public class UserManageStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户总数")
    private Long totalUserCount;

    @Schema(description = "普通用户数")
    private Long normalUserCount;

    @Schema(description = "管理员数")
    private Long adminUserCount;

    @Schema(description = "封禁用户数")
    private Long bannedUserCount;

    @Schema(description = "今日新增用户数")
    private Long todayRegisterCount;

    @Schema(description = "近 7 天新增用户数")
    private Long recentSevenDayRegisterCount;
}
