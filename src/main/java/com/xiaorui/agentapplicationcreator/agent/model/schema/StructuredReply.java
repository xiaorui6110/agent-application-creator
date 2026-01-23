package com.xiaorui.agentapplicationcreator.agent.model.schema;

import com.xiaorui.agentapplicationcreator.enums.CodeGenTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @description: 结构化回复 StructuredReply 定义强类型结构（主要是代码部分）
 * @author: xiaorui
 * @date: 2025-12-24 14:41
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredReply {

    /**
     * 应用生成模式（SINGLE_FILE / MULTI_FILE / VUE_PROJECT）
     */
    @Schema(description = "应用生成模式")
    private CodeGenTypeEnum type;

    /**
     * 是否可直接运行
     */
    @Schema(description = "是否可直接运行")
    private boolean runnable;

    /**
     * 入口文件（如 index.html）
     */
    @Schema(description = "入口文件")
    private String entry;

    /**
     * 生成的文件内容
     * key: 文件路径
     * value: 文件内容（纯文本）
     */
    @Schema(description = "生成的文件内容")
    private Map<String, String> files;

    /**
     * 对本次生成结果的简要说明（给程序或日志用）
     */
    @Schema(description = "对本次生成结果的简要说明")
    private String description;
}
