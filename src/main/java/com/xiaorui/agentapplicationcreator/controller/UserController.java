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
import com.xiaorui.agentapplicationcreator.model.dto.user.UserChangeEmailRequest;
import com.xiaorui.agentapplicationcreator.model.dto.user.UserChangePasswordRequest;
import com.xiaorui.agentapplicationcreator.model.dto.user.UserLoginRequest;
import com.xiaorui.agentapplicationcreator.model.dto.user.UserQueryRequest;
import com.xiaorui.agentapplicationcreator.model.dto.user.UserRegisterRequest;
import com.xiaorui.agentapplicationcreator.model.dto.user.UserResetPasswordRequest;
import com.xiaorui.agentapplicationcreator.model.dto.user.UserRoleUpdateRequest;
import com.xiaorui.agentapplicationcreator.model.dto.user.UserSendEmailCodeRequest;
import com.xiaorui.agentapplicationcreator.model.dto.user.UserUnbanRequest;
import com.xiaorui.agentapplicationcreator.model.dto.user.UserUpdateInfoRequest;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.UserManageStatsVO;
import com.xiaorui.agentapplicationcreator.model.vo.UserVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户使用邮箱验证码注册")
    @Parameter(name = "userRegisterRequest", description = "用户注册请求参数")
    public ServerResponseEntity<String> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtil.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        return ServerResponseEntity.success(userService.userRegister(
                userRegisterRequest.getUserEmail(),
                userRegisterRequest.getLoginPassword(),
                userRegisterRequest.getCheckPassword(),
                userRegisterRequest.getEmailVerifyCode()));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用邮箱和密码登录")
    @Parameter(name = "userLoginRequest", description = "用户登录请求参数")
    public ServerResponseEntity<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtil.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        boolean result = userService.checkPictureVerifyCode(
                userLoginRequest.getVerifyCode(), userLoginRequest.getServerVerifyCode());
        ThrowUtil.throwIf(!result, ErrorCode.PARAMS_ERROR, "验证码错误");
        return ServerResponseEntity.success(
                userService.userLogin(userLoginRequest.getUserEmail(), userLoginRequest.getLoginPassword(), request));
    }

    @PostMapping("/sendEmailCode")
    @Operation(summary = "发送邮箱验证码", description = "向目标邮箱发送验证码")
    @Parameter(name = "userSendEmailCodeRequest", description = "发送邮箱验证码请求参数")
    @RateLimit(limitType = RateLimitTypeEnum.USER, rate = 3, rateInterval = 60, message = "发送邮箱请求过于频繁，请稍后再试")
    public ServerResponseEntity<Boolean> sendEmailCode(@RequestBody UserSendEmailCodeRequest userSendEmailCodeRequest,
                                                       HttpServletRequest request) {
        ThrowUtil.throwIf(userSendEmailCodeRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        userService.sendEmailCode(userSendEmailCodeRequest.getUserEmail(), userSendEmailCodeRequest.getType(), request);
        return ServerResponseEntity.success(true);
    }

    @GetMapping("/getPictureVerifyCode")
    @Operation(summary = "获取图片验证码", description = "获取图片验证码")
    public ServerResponseEntity<Map<String, String>> getPictureVerifyCode() {
        return ServerResponseEntity.success(userService.getPictureVerifyCode());
    }

    @GetMapping("/getInfo")
    @Operation(summary = "获取用户信息", description = "获取当前用户信息")
    public ServerResponseEntity<UserVO> getUserInfo() {
        return ServerResponseEntity.success(userService.getUserInfo());
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "当前用户登出")
    public ServerResponseEntity<Boolean> userLogout(HttpServletRequest request) {
        return ServerResponseEntity.success(userService.userLogout(request));
    }

    @PostMapping("/change/email")
    @Operation(summary = "修改用户邮箱", description = "修改当前用户邮箱")
    @Parameter(name = "userChangeEmailRequest", description = "修改用户邮箱请求参数")
    public ServerResponseEntity<Boolean> changUserEmail(@RequestBody UserChangeEmailRequest userChangeEmailRequest) {
        ThrowUtil.throwIf(userChangeEmailRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        return ServerResponseEntity.success(
                userService.changeUserEmail(userChangeEmailRequest.getNewEmail(), userChangeEmailRequest.getEmailVerifyCode()));
    }

    @PostMapping("/change/password")
    @Operation(summary = "修改用户密码", description = "修改当前用户密码")
    @Parameter(name = "userChangePasswordRequest", description = "修改用户密码请求参数")
    public ServerResponseEntity<Boolean> changeUserPassword(@RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        ThrowUtil.throwIf(userChangePasswordRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        return ServerResponseEntity.success(userService.changeUserPassword(
                userChangePasswordRequest.getOldPassword(),
                userChangePasswordRequest.getNewPassword(),
                userChangePasswordRequest.getCheckPassword()));
    }

    @PostMapping("/reset/password")
    @Operation(summary = "重置用户密码", description = "重置用户密码")
    @Parameter(name = "userResetPasswordRequest", description = "重置用户密码请求参数")
    public ServerResponseEntity<Boolean> resetUserPassword(@RequestBody UserResetPasswordRequest userResetPasswordRequest) {
        ThrowUtil.throwIf(userResetPasswordRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        return ServerResponseEntity.success(userService.resetUserPassword(
                userResetPasswordRequest.getUserEmail(),
                userResetPasswordRequest.getEmailVerifyCode(),
                userResetPasswordRequest.getNewPassword(),
                userResetPasswordRequest.getCheckPassword()));
    }

    @PostMapping("/update/info")
    @Operation(summary = "更新用户信息", description = "更新当前用户信息")
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

    @PostMapping("/getInfoList")
    @Operation(summary = "用户查询", description = "根据用户 id 和昵称分页查询")
    @Parameter(name = "userQueryRequest", description = "用户查询请求参数")
    public ServerResponseEntity<Page<UserVO>> getUserInfoByIdOrName(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtil.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userInfoPage = new Page<>(current, pageSize, userPage.getTotalPage());
        userInfoPage.setRecords(getUserVOList(userPage.getRecords()));
        return ServerResponseEntity.success(userInfoPage);
    }

    @PostMapping("/update/avatar")
    @Operation(summary = "更新用户头像", description = "更新当前用户头像")
    @Parameter(name = "multipartFile", description = "头像文件")
    public ServerResponseEntity<String> updateUserAvatar(@RequestParam("multipartFile") MultipartFile multipartFile) {
        ThrowUtil.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        return ServerResponseEntity.success(userService.updateUserAvatar(multipartFile));
    }

    @PostMapping("/list/page/info")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "分页查询用户信息", description = "管理员分页查询用户信息")
    @Parameter(name = "userQueryRequest", description = "用户查询请求参数")
    public ServerResponseEntity<Page<UserVO>> listUserInfoByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtil.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userInfoPage = new Page<>(current, pageSize, userPage.getTotalPage());
        userInfoPage.setRecords(userService.getUserInfoList(userPage.getRecords()));
        return ServerResponseEntity.success(userInfoPage);
    }

    @GetMapping("/get/{userId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "根据 id 获取用户信息", description = "管理员根据 id 获取用户信息")
    @Parameter(name = "userId", description = "用户 id")
    public ServerResponseEntity<User> getUserById(@PathVariable String userId) {
        ThrowUtil.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(userId);
        ThrowUtil.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ServerResponseEntity.success(user);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "删除用户", description = "管理员删除指定用户")
    @Parameter(name = "deleteRequest", description = "删除请求参数")
    public ServerResponseEntity<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtil.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtil.throwIf(StrUtil.isBlank(deleteRequest.getId()), ErrorCode.PARAMS_ERROR, "用户 id 不能为空");
        return ServerResponseEntity.success(userService.removeById(deleteRequest.getId()));
    }

    @PostMapping("/delete/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "批量删除用户", description = "管理员批量删除用户")
    @Parameter(name = "deleteRequestList", description = "批量删除请求参数")
    public ServerResponseEntity<Boolean> deleteBatchUser(@RequestBody List<DeleteRequest> deleteRequestList) {
        ThrowUtil.throwIf(CollUtil.isEmpty(deleteRequestList), ErrorCode.PARAMS_ERROR, "参数不能为空");
        List<String> idList = deleteRequestList.stream().map(DeleteRequest::getId).collect(Collectors.toList());
        return ServerResponseEntity.success(userService.removeByIds(idList));
    }

    @PostMapping("/ban")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "封禁或解封用户", description = "管理员封禁或解封用户")
    @Parameter(name = "userUnbanRequest", description = "封禁或解封用户请求参数")
    public ServerResponseEntity<Boolean> banOrUnbanUser(@RequestBody UserUnbanRequest userUnbanRequest) {
        ThrowUtil.throwIf(userUnbanRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        User admin = userService.getById(SecurityUtil.getUserInfo().getUserId());
        return ServerResponseEntity.success(
                userService.banOrUnbanUser(userUnbanRequest.getUserId(), userUnbanRequest.getIsUnban(), admin));
    }

    @PostMapping("/update/role")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "修改用户角色", description = "管理员修改指定用户角色")
    @Parameter(name = "userRoleUpdateRequest", description = "修改用户角色请求参数")
    public ServerResponseEntity<Boolean> updateUserRole(@RequestBody UserRoleUpdateRequest userRoleUpdateRequest) {
        ThrowUtil.throwIf(userRoleUpdateRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        User admin = userService.getById(SecurityUtil.getUserInfo().getUserId());
        return ServerResponseEntity.success(
                userService.updateUserRole(userRoleUpdateRequest.getUserId(), userRoleUpdateRequest.getUserRole(), admin));
    }

    @GetMapping("/admin/stats")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "获取用户管理统计", description = "管理员获取用户管理统计数据")
    public ServerResponseEntity<UserManageStatsVO> getUserManageStats() {
        return ServerResponseEntity.success(userService.getUserManageStats());
    }

    private UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    private List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }
}
