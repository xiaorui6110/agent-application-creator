package com.xiaorui.agentapplicationcreator.model.bo;

import lombok.Data;

/**
 * @description: 用户信息在token里面的bo
 * @author: xiaorui
 * @date: 2025-11-30 14:07
 **/
@Data
public class UserInfoInTokenBO {
    /**
     * 用户id
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 用户角色 user-普通用户 admin-管理员
     */
    private String userRole;

    /**
     * 用户状态 1-正常 2-禁用
     */
    private Integer userStatus;

    /**
     * 其他Id
     */
    private Long otherId;

}