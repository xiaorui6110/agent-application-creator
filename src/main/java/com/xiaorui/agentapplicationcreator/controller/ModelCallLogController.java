package com.xiaorui.agentapplicationcreator.controller;

import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import com.xiaorui.agentapplicationcreator.model.dto.modelcall.ModelCallLogQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.ModelCallLog;
import com.xiaorui.agentapplicationcreator.model.vo.ModelCallStatsVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.ModelCallLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/modelCallLog")
public class ModelCallLogController {

    @Resource
    private ModelCallLogService modelCallLogService;

    @GetMapping("/admin/stats")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "获取模型调用统计", description = "管理员获取模型调用统计")
    public ServerResponseEntity<ModelCallStatsVO> getStats() {
        return ServerResponseEntity.success(modelCallLogService.getStats());
    }

    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "分页查询模型调用记录", description = "管理员分页查询模型调用记录")
    @Parameter(name = "queryRequest", description = "模型调用记录查询请求")
    public ServerResponseEntity<Page<ModelCallLog>> listByPage(@RequestBody ModelCallLogQueryRequest queryRequest) {
        ThrowUtil.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        Page<ModelCallLog> page = modelCallLogService.page(
                new Page<>(queryRequest.getCurrent(), queryRequest.getPageSize()),
                modelCallLogService.getQueryWrapper(queryRequest));
        return ServerResponseEntity.success(page);
    }
}
