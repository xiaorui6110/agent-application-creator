package com.xiaorui.agentapplicationcreator.constants;

/**
 * @description: 用户常量
 * @author: xiaorui
 * @date: 2025-11-30 16:46
 **/
public interface UserConstant {
    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 用户对话线程绑定状态 0-未绑定
     */
    int BIND_STATUS_UNBOUND = 0;

    /**
     * 用户对话线程绑定状态 1-已绑定
     */
    int BIND_STATUS_BOUND = 1;


}
