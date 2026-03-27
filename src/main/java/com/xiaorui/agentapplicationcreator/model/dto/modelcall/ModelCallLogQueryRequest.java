package com.xiaorui.agentapplicationcreator.model.dto.modelcall;

import com.xiaorui.agentapplicationcreator.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author xiaorui
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelCallLogQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户 id")
    private String userId;

    @Schema(description = "应用 id")
    private String appId;

    @Schema(description = "线程 id")
    private String threadId;

    @Schema(description = "Agent 名称")
    private String agentName;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "调用状态")
    private String callStatus;
}
