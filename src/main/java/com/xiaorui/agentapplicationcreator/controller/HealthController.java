package com.xiaorui.agentapplicationcreator.controller;

import com.xiaorui.agentapplicationcreator.common.BaseResponse;
import com.xiaorui.agentapplicationcreator.common.ResultUtil;
import com.xiaorui.agentapplicationcreator.constants.UserConstant;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 健康检查接口
 * @author: xiaorui
 * @date: 2025-11-29 10:44
 **/
@RestController
@RequestMapping("/")
public class HealthController {

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/health")
    public BaseResponse<String> healthCheck() {
        return ResultUtil.success( "ok");
    }

}
