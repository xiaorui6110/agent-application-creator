package com.xiaorui.agentapplicationcreator.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 用户表 实体类。
 *
 * @author xiaorui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("xr_user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户id（雪花算法）
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String userId;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 登录密码
     */
    private String loginPassword;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户性别 m-男 f-女
     */
    private String userSex;

    /**
     * 用户生日 yyyy-mm-dd
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private String userBirthday;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色 user-普通用户 admin-管理员
     */
    private String userRole;

    /**
     * 用户状态 1-正常 2-禁用
     */
    private Integer userStatus;

    /**
     * 注册ip
     */
    private String userRegip;

    /**
     * 最后登录ip
     */
    private String userLastip;

    /**
     * 最后登录时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDateTime userLasttime;

    /**
     * 用户积分
     */
    private Integer userScore;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-未删除 1-已删除（逻辑删除）
     */
    @Column(isLogicDelete = true)
    private Integer isDeleted;

}
