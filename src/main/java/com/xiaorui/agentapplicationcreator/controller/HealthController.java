package com.xiaorui.agentapplicationcreator.controller;

import com.xiaorui.agentapplicationcreator.common.BaseResponse;
import com.xiaorui.agentapplicationcreator.common.ResultUtil;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 健康检查接口
 * @author: xiaorui
 * @date: 2025-11-29 10:44
 **/
//@Tag(name = "健康检查接口")
@RestController
@RequestMapping("/")
public class HealthController {

    @GetMapping("/health")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "健康检查" , description = "健康检查")
    public BaseResponse<String> healthCheck() {
        return ResultUtil.success( "ok");
    }

}
