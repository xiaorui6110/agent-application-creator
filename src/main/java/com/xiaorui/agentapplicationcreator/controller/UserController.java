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

/**
 * @author xiaorui
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PUBLIC_PAGE_SIZE = 20;
    private static final int MAX_ADMIN_PAGE_SIZE = 100;

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "user register", description = "register user by email verification code")
    @Parameter(name = "userRegisterRequest", description = "user register request")
    public ServerResponseEntity<String> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtil.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        return ServerResponseEntity.success(userService.userRegister(
                userRegisterRequest.getUserEmail(),
                userRegisterRequest.getLoginPassword(),
                userRegisterRequest.getCheckPassword(),
                userRegisterRequest.getEmailVerifyCode()));
    }

    @PostMapping("/login")
    @Operation(summary = "user login", description = "login by email and password")
    @Parameter(name = "userLoginRequest", description = "user login request")
    public ServerResponseEntity<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtil.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        boolean result = userService.checkPictureVerifyCode(
                userLoginRequest.getVerifyCode(), userLoginRequest.getServerVerifyCode());
        ThrowUtil.throwIf(!result, ErrorCode.PARAMS_ERROR, "verification code is invalid");
        return ServerResponseEntity.success(
                userService.userLogin(userLoginRequest.getUserEmail(), userLoginRequest.getLoginPassword(), request));
    }

    @PostMapping("/sendEmailCode")
    @Operation(summary = "send email code", description = "send email verification code")
    @Parameter(name = "userSendEmailCodeRequest", description = "send email code request")
    @RateLimit(limitType = RateLimitTypeEnum.USER, rate = 3, rateInterval = 60, message = "email requests are too frequent")
    public ServerResponseEntity<Boolean> sendEmailCode(@RequestBody UserSendEmailCodeRequest userSendEmailCodeRequest,
                                                       HttpServletRequest request) {
        ThrowUtil.throwIf(userSendEmailCodeRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        userService.sendEmailCode(userSendEmailCodeRequest.getUserEmail(), userSendEmailCodeRequest.getType(), request);
        return ServerResponseEntity.success(true);
    }

    @GetMapping("/getPictureVerifyCode")
    @Operation(summary = "get picture verification code", description = "get picture verification code")
    public ServerResponseEntity<Map<String, String>> getPictureVerifyCode() {
        return ServerResponseEntity.success(userService.getPictureVerifyCode());
    }

    @GetMapping("/getInfo")
    @Operation(summary = "get current user info", description = "get current user info")
    public ServerResponseEntity<UserVO> getUserInfo() {
        return ServerResponseEntity.success(userService.getUserInfo());
    }

    @PostMapping("/logout")
    @Operation(summary = "user logout", description = "logout current user")
    public ServerResponseEntity<Boolean> userLogout(HttpServletRequest request) {
        return ServerResponseEntity.success(userService.userLogout(request));
    }

    @PostMapping("/change/email")
    @Operation(summary = "change user email", description = "change current user email")
    @Parameter(name = "userChangeEmailRequest", description = "change user email request")
    public ServerResponseEntity<Boolean> changUserEmail(@RequestBody UserChangeEmailRequest userChangeEmailRequest) {
        ThrowUtil.throwIf(userChangeEmailRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        return ServerResponseEntity.success(
                userService.changeUserEmail(userChangeEmailRequest.getNewEmail(), userChangeEmailRequest.getEmailVerifyCode()));
    }

    @PostMapping("/change/password")
    @Operation(summary = "change user password", description = "change current user password")
    @Parameter(name = "userChangePasswordRequest", description = "change user password request")
    public ServerResponseEntity<Boolean> changeUserPassword(@RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        ThrowUtil.throwIf(userChangePasswordRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        return ServerResponseEntity.success(userService.changeUserPassword(
                userChangePasswordRequest.getOldPassword(),
                userChangePasswordRequest.getNewPassword(),
                userChangePasswordRequest.getCheckPassword()));
    }

    @PostMapping("/reset/password")
    @Operation(summary = "reset user password", description = "reset user password")
    @Parameter(name = "userResetPasswordRequest", description = "reset user password request")
    public ServerResponseEntity<Boolean> resetUserPassword(@RequestBody UserResetPasswordRequest userResetPasswordRequest) {
        ThrowUtil.throwIf(userResetPasswordRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        return ServerResponseEntity.success(userService.resetUserPassword(
                userResetPasswordRequest.getUserEmail(),
                userResetPasswordRequest.getEmailVerifyCode(),
                userResetPasswordRequest.getNewPassword(),
                userResetPasswordRequest.getCheckPassword()));
    }

    @PostMapping("/update/info")
    @Operation(summary = "update user info", description = "update current user info")
    @Parameter(name = "userUpdateInfoRequest", description = "update user info request")
    public ServerResponseEntity<Boolean> updateUserInfo(@RequestBody UserUpdateInfoRequest userUpdateInfoRequest) {
        ThrowUtil.throwIf(userUpdateInfoRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        String userId = SecurityUtil.getUserInfo().getUserId();
        if (!userId.equals(userUpdateInfoRequest.getUserId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "can only update current user");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateInfoRequest, user);
        user.setUpdateTime(LocalDateTime.now());
        return ServerResponseEntity.success(userService.updateById(user));
    }

    @PostMapping("/getInfoList")
    @Operation(summary = "list users", description = "query users by id or nickname")
    @Parameter(name = "userQueryRequest", description = "user query request")
    public ServerResponseEntity<Page<UserVO>> getUserInfoByIdOrName(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtil.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        normalizePageRequest(userQueryRequest, MAX_PUBLIC_PAGE_SIZE);
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userInfoPage = new Page<>(current, pageSize, userPage.getTotalRow());
        userInfoPage.setRecords(getUserVOList(userPage.getRecords()));
        return ServerResponseEntity.success(userInfoPage);
    }

    @PostMapping("/update/avatar")
    @Operation(summary = "update user avatar", description = "update current user avatar")
    @Parameter(name = "multipartFile", description = "avatar file")
    public ServerResponseEntity<String> updateUserAvatar(@RequestParam("multipartFile") MultipartFile multipartFile) {
        ThrowUtil.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "file is blank");
        return ServerResponseEntity.success(userService.updateUserAvatar(multipartFile));
    }

    @PostMapping("/list/page/info")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "list users by page", description = "admin list users by page")
    @Parameter(name = "userQueryRequest", description = "user query request")
    public ServerResponseEntity<Page<UserVO>> listUserInfoByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtil.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        normalizePageRequest(userQueryRequest, MAX_ADMIN_PAGE_SIZE);
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userInfoPage = new Page<>(current, pageSize, userPage.getTotalRow());
        userInfoPage.setRecords(userService.getUserInfoList(userPage.getRecords()));
        return ServerResponseEntity.success(userInfoPage);
    }

    @GetMapping("/get/{userId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "get user by id", description = "admin get user by id")
    @Parameter(name = "userId", description = "user id")
    public ServerResponseEntity<User> getUserById(@PathVariable String userId) {
        ThrowUtil.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(userId);
        ThrowUtil.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ServerResponseEntity.success(user);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "delete user", description = "admin delete a user")
    @Parameter(name = "deleteRequest", description = "delete request")
    public ServerResponseEntity<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtil.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        ThrowUtil.throwIf(StrUtil.isBlank(deleteRequest.getId()), ErrorCode.PARAMS_ERROR, "user id is blank");
        return ServerResponseEntity.success(userService.removeById(deleteRequest.getId()));
    }

    @PostMapping("/delete/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "batch delete users", description = "admin batch delete users")
    @Parameter(name = "deleteRequestList", description = "batch delete request list")
    public ServerResponseEntity<Boolean> deleteBatchUser(@RequestBody List<DeleteRequest> deleteRequestList) {
        ThrowUtil.throwIf(CollUtil.isEmpty(deleteRequestList), ErrorCode.PARAMS_ERROR, "request is blank");
        List<String> idList = deleteRequestList.stream().map(DeleteRequest::getId).collect(Collectors.toList());
        return ServerResponseEntity.success(userService.removeByIds(idList));
    }

    @PostMapping("/ban")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "ban or unban user", description = "admin ban or unban user")
    @Parameter(name = "userUnbanRequest", description = "ban or unban user request")
    public ServerResponseEntity<Boolean> banOrUnbanUser(@RequestBody UserUnbanRequest userUnbanRequest) {
        ThrowUtil.throwIf(userUnbanRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        User admin = userService.getById(SecurityUtil.getUserInfo().getUserId());
        return ServerResponseEntity.success(
                userService.banOrUnbanUser(userUnbanRequest.getUserId(), userUnbanRequest.getIsUnban(), admin));
    }

    @PostMapping("/update/role")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "update user role", description = "admin update user role")
    @Parameter(name = "userRoleUpdateRequest", description = "update user role request")
    public ServerResponseEntity<Boolean> updateUserRole(@RequestBody UserRoleUpdateRequest userRoleUpdateRequest) {
        ThrowUtil.throwIf(userRoleUpdateRequest == null, ErrorCode.PARAMS_ERROR, "request is blank");
        User admin = userService.getById(SecurityUtil.getUserInfo().getUserId());
        return ServerResponseEntity.success(
                userService.updateUserRole(userRoleUpdateRequest.getUserId(), userRoleUpdateRequest.getUserRole(), admin));
    }

    @GetMapping("/admin/stats")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "get user manage stats", description = "admin get user management stats")
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

    private void normalizePageRequest(UserQueryRequest userQueryRequest, int maxPageSize) {
        if (userQueryRequest.getCurrent() <= 0) {
            userQueryRequest.setCurrent(1);
        }
        if (userQueryRequest.getPageSize() <= 0) {
            userQueryRequest.setPageSize(DEFAULT_PAGE_SIZE);
            return;
        }
        ThrowUtil.throwIf(userQueryRequest.getPageSize() > maxPageSize, ErrorCode.PARAMS_ERROR, "pageSize exceeds limit");
    }
}
