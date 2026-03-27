package com.xiaorui.agentapplicationcreator.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ModelCallStatsVO {

    @Schema(description = "调用总数")
    private Long totalCallCount;

    @Schema(description = "今日调用数")
    private Long todayCallCount;

    @Schema(description = "成功调用数")
    private Long successCallCount;

    @Schema(description = "失败调用数")
    private Long failedCallCount;

    @Schema(description = "输入 token 总量")
    private Long totalPromptTokens;

    @Schema(description = "输出 token 总量")
    private Long totalCompletionTokens;

    @Schema(description = "总 token 量")
    private Long totalTokens;

    @Schema(description = "平均耗时 ms")
    private Long avgLatencyMs;
}
