package com.xiaorui.agentapplicationcreator.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.paginate.Page;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.model.dto.user.*;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.TokenInfoVO;
import com.xiaorui.agentapplicationcreator.model.vo.UserVO;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.utils.SecurityUtil;
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
        String userId = userService.userRegister(userEmail, loginPassword, checkPassword, emailVerifyCode);
        return ServerResponseEntity.success(userId);
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
        TokenInfoVO tokenInfoVO = userService.userLogin(userEmail, loginPassword);
        return ServerResponseEntity.success(tokenInfoVO);
    }

    /**
     * 发送邮箱验证码（点击向目标邮箱发送验证码）
     */
    @PostMapping("/sendEmailCode")
    public ServerResponseEntity<String> sendEmailCode(@RequestBody UserSendEmailCodeRequest userSendEmailCodeRequest, HttpServletRequest request ) {
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
        Map<String, String> pictureVerifyCode = userService.getPictureVerifyCode();
        return ServerResponseEntity.success(pictureVerifyCode);
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
        boolean result = userService.userLogout(request);
        return ServerResponseEntity.success(result);
    }

    /**
     * 修改用户邮箱
     */
    @PostMapping("/changeEmail")
    public ServerResponseEntity<Boolean> changUserEmail(@RequestBody UserChangeEmailRequest userChangeEmailRequest) {
        ThrowUtil.throwIf(userChangeEmailRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String newEmail = userChangeEmailRequest.getNewEmail();
        String emailVerifyCode = userChangeEmailRequest.getEmailVerifyCode();
        boolean result = userService.changeUserEmail(newEmail, emailVerifyCode);
        return ServerResponseEntity.success(result);
    }

    /**
     * 修改用户密码
     */
    @PostMapping("/changePassword")
    public ServerResponseEntity<Boolean> changeUserPassword(@RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        ThrowUtil.throwIf(userChangePasswordRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String oldPassword = userChangePasswordRequest.getOldPassword();
        String newPassword = userChangePasswordRequest.getNewPassword();
        String checkPassword = userChangePasswordRequest.getCheckPassword();
        boolean result = userService.changeUserPassword(oldPassword, newPassword, checkPassword);
        return ServerResponseEntity.success(result);
    }

    /**
     * 重置用户密码（用户忘记密码的情况下）
     */
    @PostMapping("/resetPassword")
    public ServerResponseEntity<Boolean> resetUserPassword(@RequestBody UserResetPasswordRequest userResetPasswordRequest) {
        ThrowUtil.throwIf(userResetPasswordRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String userEmail = userResetPasswordRequest.getUserEmail();
        String emailVerifyCode = userResetPasswordRequest.getEmailVerifyCode();
        String newPassword = userResetPasswordRequest.getNewPassword();
        String checkPassword = userResetPasswordRequest.getCheckPassword();
        boolean result = userService.resetUserPassword(userEmail, emailVerifyCode, newPassword, checkPassword);
        return ServerResponseEntity.success(result);
    }

    /**
     * 更新用户信息（只能更新自己的信息）
     */
    @PostMapping("/updateInfo")
    public ServerResponseEntity<Boolean> updateUserInfo(@RequestBody UserUpdateInfoRequest userUpdateInfoRequest) {
        ThrowUtil.throwIf(userUpdateInfoRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        String userId = SecurityUtil.getUser().getUserId();
        if (!userId.equals(userUpdateInfoRequest.getUserId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能更新自己的信息");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateInfoRequest, user);
        user.setUpdateTime(LocalDateTime.now());
        boolean result = userService.updateById(user);
        return ServerResponseEntity.success(result);
    }

    /**
     * 用户查询请求（根据用户id、昵称查询，分页获取）
     */
    @PostMapping("/getVOList")
    public ServerResponseEntity<Page<UserVO>> getUserVOByIdOrName(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtil.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotalPage());
        List<UserVO> userVOList = getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ServerResponseEntity.success(userVOPage);
    }

    /**
     * 更新用户头像（本地文件上传）
     */
    @PostMapping("/updateAvatar")
    public ServerResponseEntity<String> updateUserAvatar(MultipartFile multipartFile) {
        String result = userService.updateUserAvatar(multipartFile);
        return ServerResponseEntity.success(result);
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
