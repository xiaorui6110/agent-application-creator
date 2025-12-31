package com.xiaorui.agentapplicationcreator.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.common.DeleteRequest;
import com.xiaorui.agentapplicationcreator.constants.UserConstant;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.authority.annotation.AuthCheck;
import com.xiaorui.agentapplicationcreator.model.dto.user.*;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.TokenInfoVO;
import com.xiaorui.agentapplicationcreator.model.vo.UserVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
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
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册（邮箱验证码注册）
     */
    @PostMapping("/register")
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
    public ServerResponseEntity<TokenInfoVO> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtil.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String userEmail = userLoginRequest.getUserEmail();
        String loginPassword = userLoginRequest.getLoginPassword();
        String verifyCode = userLoginRequest.getVerifyCode();
        String serverVerifyCode = userLoginRequest.getServerVerifyCode();
        // 校验图形数字验证码（从登录逻辑中抽离出来了）
        boolean result = userService.checkPictureVerifyCode(verifyCode, serverVerifyCode);
        ThrowUtil.throwIf(!result, ErrorCode.PARAMS_ERROR, "验证码错误");
        return ServerResponseEntity.success(userService.userLogin(userEmail, loginPassword));
    }

    /**
     * 发送邮箱验证码（点击向目标邮箱发送验证码）
     */
    @PostMapping("/sendEmailCode")
    public ServerResponseEntity<String> sendEmailCode(@RequestBody UserSendEmailCodeRequest userSendEmailCodeRequest,
                                                      HttpServletRequest request ) {
        ThrowUtil.throwIf(userSendEmailCodeRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String userEmail = userSendEmailCodeRequest.getUserEmail();
        String type = userSendEmailCodeRequest.getType();
        userService.sendEmailCode(userEmail, type, request);
        return ServerResponseEntity.success();
    }

    /**
     * 获取图形验证码（直接展示在前端）
     */
    @GetMapping("/getPictureVerifyCode")
    public ServerResponseEntity<Map<String, String>> getPictureVerifyCode() {
        return ServerResponseEntity.success(userService.getPictureVerifyCode());
    }

    /**
     * 获取用户信息（只能查看自己的个人信息）
     */
    @GetMapping("/getInfo")
    public ServerResponseEntity<UserVO> getUserInfo() {
        return ServerResponseEntity.success(userService.getUserInfo());
    }

    /**
     * 用户登出（退出登录）
     */
    @PostMapping("/logout")
    public ServerResponseEntity<Boolean> userLogout(HttpServletRequest request) {
        return ServerResponseEntity.success(userService.userLogout(request));
    }

    /**
     * 修改用户邮箱
     */
    @PostMapping("/change/email")
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
    public ServerResponseEntity<String> updateUserAvatar(@RequestBody UserUpdateAvatarRequest userUpdateAvatarRequest) {
        ThrowUtil.throwIf(userUpdateAvatarRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        MultipartFile multipartFile = userUpdateAvatarRequest.getMultipartFile();
        return ServerResponseEntity.success(userService.updateUserAvatar(multipartFile));
    }

    /**
     * 【管理员】分页查询用户信息
     */
    @PostMapping("/list/page/info")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
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
