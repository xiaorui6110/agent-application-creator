package com.xiaorui.agentapplicationcreator.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.common.DeleteRequest;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import com.xiaorui.agentapplicationcreator.manager.ratelimit.RateLimit;
import com.xiaorui.agentapplicationcreator.manager.ratelimit.RateLimitTypeEnum;
import com.xiaorui.agentapplicationcreator.model.dto.user.*;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.UserVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户表 控制层。
 *
 * @author xiaorui
 */
//@Tag(name = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册（邮箱验证码注册）
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册" , description = "用户使用邮箱验证码注册")
    @Parameter(name = "userRegisterRequest", description = "用户注册请求参数")
    public ServerResponseEntity<String> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtil.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String userEmail = userRegisterRequest.getUserEmail();
        String loginPassword = userRegisterRequest.getLoginPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String emailVerifyCode = userRegisterRequest.getEmailVerifyCode();
        return ServerResponseEntity.success(
                userService.userRegister(userEmail, loginPassword, checkPassword, emailVerifyCode));
    }

    /**
     * 用户登录（邮箱 + 密码）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录" , description = "用户使用邮箱 + 密码登录")
    @Parameter(name = "userLoginRequest", description = "用户登录请求参数")
    public ServerResponseEntity<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtil.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String userEmail = userLoginRequest.getUserEmail();
        String loginPassword = userLoginRequest.getLoginPassword();
        String verifyCode = userLoginRequest.getVerifyCode();
        String serverVerifyCode = userLoginRequest.getServerVerifyCode();
        // 校验图形数字验证码（从登录逻辑中抽离出来了）
        boolean result = userService.checkPictureVerifyCode(verifyCode, serverVerifyCode);
        ThrowUtil.throwIf(!result, ErrorCode.PARAMS_ERROR, "验证码错误");
        // 内置的 PasswordCheckManager 是半小时内最多错误 10 次，之后锁定账号
        return ServerResponseEntity.success(userService.userLogin(userEmail, loginPassword, request));
    }

    /**
     * 发送邮箱验证码（点击向目标邮箱发送验证码）（用户在 60؜ 秒内最多只能发起 3 发送邮箱请求）（未登录则降级为IP）
     */
    @PostMapping("/sendEmailCode")
    @Operation(summary = "发送邮箱验证码" , description = "向目标邮箱发送验证码")
    @Parameter(name = "userSendEmailCodeRequest", description = "发送邮箱验证码请求参数")
    @RateLimit(limitType = RateLimitTypeEnum.USER, rate = 3, rateInterval = 60, message = "发送邮箱请求过于频繁，请稍后再试")
    public ServerResponseEntity<Boolean> sendEmailCode(@RequestBody UserSendEmailCodeRequest userSendEmailCodeRequest,
                                                       HttpServletRequest request ) {
        ThrowUtil.throwIf(userSendEmailCodeRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String userEmail = userSendEmailCodeRequest.getUserEmail();
        String type = userSendEmailCodeRequest.getType();
        userService.sendEmailCode(userEmail, type, request);
        return ServerResponseEntity.success(true);
    }

    /**
     * 获取图形验证码（直接展示在前端）
     */
    @GetMapping("/getPictureVerifyCode")
    @Operation(summary = "获取图形验证码" , description = "获取图形验证码")
    public ServerResponseEntity<Map<String, String>> getPictureVerifyCode() {
        return ServerResponseEntity.success(userService.getPictureVerifyCode());
    }

    /**
     * 获取用户信息（只能查看自己的个人信息）
     */
    @GetMapping("/getInfo")
    @Operation(summary = "获取用户信息" , description = "获取当前用户信息")
    public ServerResponseEntity<UserVO> getUserInfo() {
        return ServerResponseEntity.success(userService.getUserInfo());
    }

    /**
     * 用户登出（退出登录）
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出" , description = "当前用户登出")
    public ServerResponseEntity<Boolean> userLogout(HttpServletRequest request) {
        return ServerResponseEntity.success(userService.userLogout(request));
    }

    /**
     * 修改用户邮箱
     */
    @PostMapping("/change/email")
    @Operation(summary = "修改用户邮箱" , description = "修改当前用户邮箱")
    @Parameter(name = "userChangeEmailRequest", description = "修改用户邮箱请求参数")
    public ServerResponseEntity<Boolean> changUserEmail(@RequestBody UserChangeEmailRequest userChangeEmailRequest) {
        ThrowUtil.throwIf(userChangeEmailRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String newEmail = userChangeEmailRequest.getNewEmail();
        String emailVerifyCode = userChangeEmailRequest.getEmailVerifyCode();
        return ServerResponseEntity.success(userService.changeUserEmail(newEmail, emailVerifyCode));
    }

    /**
     * 修改用户密码
     */
    @PostMapping("/change/password")
    @Operation(summary = "修改用户密码" , description = "修改当前用户密码")
    @Parameter(name = "userChangePasswordRequest", description = "修改用户密码请求参数")
    public ServerResponseEntity<Boolean> changeUserPassword(@RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        ThrowUtil.throwIf(userChangePasswordRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String oldPassword = userChangePasswordRequest.getOldPassword();
        String newPassword = userChangePasswordRequest.getNewPassword();
        String checkPassword = userChangePasswordRequest.getCheckPassword();
        return ServerResponseEntity.success(userService.changeUserPassword(oldPassword, newPassword, checkPassword));
    }

    /**
     * 重置用户密码（用户忘记密码的情况下）
     */
    @PostMapping("/reset/password")
    @Operation(summary = "重置用户密码" , description = "重置当前用户密码")
    @Parameter(name = "userResetPasswordRequest", description = "重置用户密码请求参数")
    public ServerResponseEntity<Boolean> resetUserPassword(@RequestBody UserResetPasswordRequest userResetPasswordRequest) {
        ThrowUtil.throwIf(userResetPasswordRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String userEmail = userResetPasswordRequest.getUserEmail();
        String emailVerifyCode = userResetPasswordRequest.getEmailVerifyCode();
        String newPassword = userResetPasswordRequest.getNewPassword();
        String checkPassword = userResetPasswordRequest.getCheckPassword();
        return ServerResponseEntity.success(
                userService.resetUserPassword(userEmail, emailVerifyCode, newPassword, checkPassword));
    }

    /**
     * 更新用户信息（只能更新自己的信息）
     */
    @PostMapping("/update/info")
    @Operation(summary = "更新用户信息" , description = "更新当前用户信息")
    @Parameter(name = "userUpdateInfoRequest", description = "更新用户信息请求参数")
    public ServerResponseEntity<Boolean> updateUserInfo(@RequestBody UserUpdateInfoRequest userUpdateInfoRequest) {
        ThrowUtil.throwIf(userUpdateInfoRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String userId = SecurityUtil.getUserInfo().getUserId();
        if (!userId.equals(userUpdateInfoRequest.getUserId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能更新自己的信息");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateInfoRequest, user);
        user.setUpdateTime(LocalDateTime.now());
        return ServerResponseEntity.success(userService.updateById(user));
    }

    /**
     * 用户查询请求（根据用户id、昵称查询，分页获取）
     */
    @PostMapping("/getInfoList")
    @Operation(summary = "用户查询" , description = "用户根据用户id、昵称，分页获取查询用户信息")
    @Parameter(name = "userQueryRequest", description = "用户查询请求参数")
    public ServerResponseEntity<Page<UserVO>> getUserInfoByIdOrName(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtil.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(
                new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userInfoPage = new Page<>(current, pageSize, userPage.getTotalPage());
        List<UserVO> userInfoList = getUserVOList(userPage.getRecords());
        userInfoPage.setRecords(userInfoList);
        return ServerResponseEntity.success(userInfoPage);
    }

    /**
     * 更新用户头像（本地文件上传）
     */
    @PostMapping("/update/avatar")
    @Operation(summary = "更新用户头像" , description = "更新当前用户头像")
    @Parameter(name = "头像文件", description = "头像文件")
    public ServerResponseEntity<String> updateUserAvatar(@RequestParam("multipartFile") MultipartFile multipartFile) {
        ThrowUtil.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        return ServerResponseEntity.success(userService.updateUserAvatar(multipartFile));
    }

    /**
     * 【管理员】分页查询用户信息
     */
    @PostMapping("/list/page/info")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "分页查询用户信息" , description = "管理员分页查询用户信息")
    @Parameter(name = "userQueryRequest", description = "用户查询请求参数")
    public ServerResponseEntity<Page<UserVO>> listUserInfoByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtil.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userInfoPage = new Page<>(current, pageSize, userPage.getTotalPage());
        List<UserVO> userInfoList = userService.getUserInfoList(userPage.getRecords());
        userInfoPage.setRecords(userInfoList);
        return ServerResponseEntity.success(userInfoPage);
    }


    /**
     * 【管理员】根据 id 获取用户信息
     */
    @GetMapping("/get/{userId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "根据 id 获取用户信息" , description = "管理员根据 id 获取用户信息")
    @Parameter(name = "userId", description = "用户id")
    public ServerResponseEntity<User> getUserById(@PathVariable String userId) {
        ThrowUtil.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(userId);
        ThrowUtil.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ServerResponseEntity.success(user);
    }

    /**
     * 【管理员】删除用户
     * @param deleteRequest 删除请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "删除用户" , description = "管理员删除指定用户")
    @Parameter(name = "deleteRequest", description = "删除请求参数")
    public ServerResponseEntity<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtil.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtil.throwIf(StrUtil.isBlank(deleteRequest.getId()), ErrorCode.PARAMS_ERROR, "用户id不能为空");
        return ServerResponseEntity.success(userService.removeById(deleteRequest.getId()));
    }

    /**
     * 【管理员】批量删除用户
     */
    @PostMapping("/delete/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "批量删除用户" , description = "管理员批量删除用户")
    @Parameter(name = "deleteRequestList", description = "批量删除请求参数")
    public ServerResponseEntity<Boolean> deleteBatchUser(@RequestBody List<DeleteRequest> deleteRequestList) {
        ThrowUtil.throwIf(CollUtil.isEmpty(deleteRequestList), ErrorCode.PARAMS_ERROR, "参数不能为空");
        List<String> idList = deleteRequestList.stream()
                .map(DeleteRequest::getId)
                .collect(Collectors.toList());
        return ServerResponseEntity.success(userService.removeByIds(idList));
    }

    /**
     * 【管理员】封禁或解封用户
     */
    @PostMapping("/ban")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "封禁或解封用户" , description = "管理员封禁或解封用户")
    @Parameter(name = "userUnbanRequest", description = "封禁或解封用户请求参数")
    public ServerResponseEntity<Boolean> banOrUnbanUser(@RequestBody UserUnbanRequest userUnbanRequest) {
        ThrowUtil.throwIf(userUnbanRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        User admin = userService.getById(SecurityUtil.getUserInfo().getUserId());
        return ServerResponseEntity.success(
                userService.banOrUnbanUser(userUnbanRequest.getUserId(), userUnbanRequest.getIsUnban(), admin));
    }

    /**
     * 获得脱敏后的用户信息
     */
    private UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户信息列表
     */
    private List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

}
