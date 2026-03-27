package com.xiaorui.agentapplicationcreator.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.constant.CrawlerConstant;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.enums.SysTypeEnum;
import com.xiaorui.agentapplicationcreator.enums.UserRoleEnum;
import com.xiaorui.agentapplicationcreator.enums.UserStatusEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.manager.minio.FileManager;
import com.xiaorui.agentapplicationcreator.manager.password.PasswordCheckManager;
import com.xiaorui.agentapplicationcreator.manager.token.TokenStoreManager;
import com.xiaorui.agentapplicationcreator.mapper.UserMapper;
import com.xiaorui.agentapplicationcreator.model.bo.UserInfoInTokenBO;
import com.xiaorui.agentapplicationcreator.model.dto.file.UploadPictureResult;
import com.xiaorui.agentapplicationcreator.model.dto.user.UserQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.UserManageStatsVO;
import com.xiaorui.agentapplicationcreator.model.vo.UserVO;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.EmailSenderUtil;
import com.xiaorui.agentapplicationcreator.util.PrincipalUtil;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xiaorui.agentapplicationcreator.constant.LogicDeletedConstant.LOGIC_DELETED_NO;
import static com.xiaorui.agentapplicationcreator.constant.UserConstant.USER_LOGIN_STATE;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final int USERPASSWORD_MIN_LENGTH = 6;
    private static final int USERPASSWORD_MAX_LENGTH = 36;
    private static final int IP_REQUEST_LIMIT = 5;
    private static final int EMAIL_REQUEST_LIMIT = 3;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private EmailSenderUtil emailSenderUtil;

    @Resource
    private PasswordCheckManager passwordCheckManager;

    @Resource
    private TokenStoreManager tokenStoreManager;

    @Resource
    private FileManager fileManager;

    @Override
    public String userRegister(String userEmail, String loginPassword, String checkPassword, String emailVerifyCode) {
        if (StrUtil.hasBlank(userEmail, loginPassword, checkPassword, emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (!PrincipalUtil.isEmail(userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
        if (loginPassword.length() < USERPASSWORD_MIN_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        if (loginPassword.length() > USERPASSWORD_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过长");
        }
        if (!loginPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        String verifyCodeKey = String.format("email:code:verify:register:%s", userEmail);
        String correctCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
        if (StrUtil.isBlank(correctCode) || !correctCode.equals(emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        synchronized (userEmail.intern()) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_email", userEmail);
            long count = this.mapper.selectCountByQuery(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被注册");
            }
            User user = new User();
            user.setNickName(userEmail.substring(0, userEmail.indexOf("@")));
            user.setUserEmail(userEmail);
            user.setLoginPassword(passwordEncoder.encode(loginPassword));
            user.setUserRole(UserConstant.DEFAULT_ROLE);
            user.setUserStatus(UserStatusEnum.NORMAL.getValue());
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            boolean result = this.save(user);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户注册失败");
            }
            log.info("user register success: {}", user.getUserId());
            stringRedisTemplate.delete(verifyCodeKey);
            return user.getUserId();
        }
    }

    @Override
    public UserVO userLogin(String userEmail, String loginPassword, HttpServletRequest request) {
        if (StrUtil.hasBlank(userEmail, loginPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (!PrincipalUtil.isEmail(userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
        if (loginPassword.length() < USERPASSWORD_MIN_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        if (loginPassword.length() > USERPASSWORD_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过长");
        }
        User user = this.mapper.selectOneByQuery(new QueryWrapper().eq(User::getUserEmail, userEmail));
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if (Objects.equals(user.getUserStatus(), UserStatusEnum.BANNED.getValue())
                || CrawlerConstant.BAN_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户已被封禁，请联系管理员");
        }
        passwordCheckManager.checkPassword(SysTypeEnum.ORDINARY, user.getUserEmail(), loginPassword, user.getLoginPassword());

        UserInfoInTokenBO userInfoInTokenBO = new UserInfoInTokenBO();
        userInfoInTokenBO.setUserId(user.getUserId());
        userInfoInTokenBO.setNickName(user.getNickName());
        userInfoInTokenBO.setUserRole(user.getUserRole());
        userInfoInTokenBO.setUserStatus(user.getUserStatus());
        tokenStoreManager.storeAndGetVo(userInfoInTokenBO);

        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        request.getSession().setAttribute(USER_LOGIN_STATE, userVO);
        log.info("user login success: {}", user.getUserId());
        return userVO;
    }

    @Override
    public boolean sendEmailCode(String userEmail, String type, HttpServletRequest request) {
        if (StrUtil.hasBlank(userEmail, type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (!PrincipalUtil.isEmail(userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
        String clientIp = request.getRemoteAddr();
        String ipKey = String.format("email:code:ip:%s", clientIp);
        String emailKey = String.format("email:code:email:%s", userEmail);
        String ipCount = stringRedisTemplate.opsForValue().get(ipKey);
        if (StrUtil.isNotBlank(ipCount) && Integer.parseInt(ipCount) > IP_REQUEST_LIMIT) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "请求验证码过于频繁，请稍后再试");
        }
        String emailCount = stringRedisTemplate.opsForValue().get(emailKey);
        if (StrUtil.isNotBlank(emailCount) && Integer.parseInt(emailCount) > EMAIL_REQUEST_LIMIT) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "该邮箱请求验证码过于频繁，请稍后再试");
        }
        String code = RandomUtil.randomNumbers(6);
        try {
            emailSenderUtil.sendEmail(userEmail, code);
        } catch (Exception e) {
            log.error("fail to send email", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送验证码失败");
        }
        stringRedisTemplate.opsForValue().increment(ipKey, 1);
        stringRedisTemplate.opsForValue().increment(emailKey, 1);
        stringRedisTemplate.expire(ipKey, 1, TimeUnit.HOURS);
        stringRedisTemplate.expire(emailKey, 1, TimeUnit.HOURS);
        String verifyCodeKey = String.format("email:code:verify:%s:%s", type, userEmail);
        stringRedisTemplate.opsForValue().set(verifyCodeKey, code, 5, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public Map<String, String> getPictureVerifyCode() {
        String characters = "0123456789";
        RandomGenerator randomGenerator = new RandomGenerator(characters, 4);
        ShearCaptcha shearCaptcha = CaptchaUtil.createShearCaptcha(320, 100, 4, 4);
        shearCaptcha.setGenerator(randomGenerator);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        shearCaptcha.write(outputStream);
        byte[] captchaBytes = outputStream.toByteArray();
        String base64Captcha = Base64.getEncoder().encodeToString(captchaBytes);
        String captchaCode = shearCaptcha.getCode();
        stringRedisTemplate.opsForValue().set("captcha:" + captchaCode, captchaCode, 300, TimeUnit.SECONDS);
        Map<String, String> data = new HashMap<>();
        data.put("base64Captcha", base64Captcha);
        data.put("encryptedCaptcha", captchaCode);
        return data;
    }

    @Override
    public boolean checkPictureVerifyCode(String verifyCode, String serverVerifyCode) {
        if (verifyCode != null && serverVerifyCode != null && verifyCode.equals(serverVerifyCode)) {
            return true;
        }
        log.info("fail to check verifyCode");
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtil.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        return QueryWrapper.create()
                .eq("user_id", userQueryRequest.getUserId(), StrUtil.isNotBlank(userQueryRequest.getUserId()))
                .like("nick_name", userQueryRequest.getNickName(), StrUtil.isNotBlank(userQueryRequest.getNickName()))
                .eq("is_deleted", LOGIC_DELETED_NO)
                .orderBy("nick_name");
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        String accessToken = request.getHeader("Authorization");
        if (StrUtil.isBlank(accessToken)) {
            accessToken = StpUtil.getTokenValue();
        }
        if (StrUtil.isBlank(accessToken)) {
            return true;
        }
        tokenStoreManager.deleteCurrentToken(accessToken);
        log.info("user logout success");
        return true;
    }

    @Override
    public UserVO getUserInfo() {
        String userId = SecurityUtil.getUserInfo().getUserId();
        User user = this.mapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return getUserInfo(user);
    }

    @Override
    public UserVO getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        UserVO currentUserVO = (UserVO) userObj;
        if (currentUserVO == null || currentUserVO.getUserId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        User user = this.getById(currentUserVO.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return getUserInfo(user);
    }

    @Override
    public UserVO getUserInfo(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserInfoList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserInfo).collect(Collectors.toList());
    }

    @Override
    public boolean changeUserEmail(String newUserEmail, String emailVerifyCode) {
        if (StrUtil.hasBlank(newUserEmail, emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request params is blank");
        }
        if (!PrincipalUtil.isEmail(newUserEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid email format");
        }
        String verifyCodeKey = String.format("email:code:verify:changeEmail:%s", newUserEmail);
        String correctCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
        if (StrUtil.isBlank(correctCode) || !correctCode.equals(emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid or expired verify code");
        }
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = this.mapper.selectOneById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "user not found");
        }
        synchronized (newUserEmail.intern()) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_email", newUserEmail);
            long count = this.mapper.selectCountByQuery(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "email already exists");
            }
            User user = new User();
            user.setUserId(loginUser.getUserId());
            user.setUserEmail(newUserEmail);
            user.setUpdateTime(LocalDateTime.now());
            boolean updateResult = this.updateById(user);
            if (!updateResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "failed to change email");
            }
            log.info("user change email success : {}", userId);
            stringRedisTemplate.delete(verifyCodeKey);
            return true;
        }
    }

    @Override
    public boolean changeUserPassword(String oldPassword, String newPassword, String checkPassword) {
        if (StrUtil.hasBlank(oldPassword, newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request params is blank");
        }
        if (newPassword.length() < USERPASSWORD_MIN_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "password is too short");
        }
        if (newPassword.length() > USERPASSWORD_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "password is too long");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "password confirmation mismatch");
        }
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = this.mapper.selectOneById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "user not found");
        }
        if (!passwordEncoder.matches(oldPassword, loginUser.getLoginPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "old password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, loginUser.getLoginPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "new password must differ from old password");
        }
        loginUser.setLoginPassword(passwordEncoder.encode(newPassword));
        loginUser.setUpdateTime(LocalDateTime.now());
        boolean updateResult = this.updateById(loginUser);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "failed to change password");
        }
        tokenStoreManager.deleteAllToken(String.valueOf(loginUser.getUserStatus()), userId);
        log.info("user change password success : {}", userId);
        return true;
    }

    @Override
    public boolean resetUserPassword(String userEmail, String emailVerifyCode, String newPassword, String checkPassword) {
        if (StrUtil.hasBlank(userEmail, emailVerifyCode, newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request params is blank");
        }
        if (!PrincipalUtil.isEmail(userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid email format");
        }
        if (newPassword.length() < USERPASSWORD_MIN_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "password is too short");
        }
        if (newPassword.length() > USERPASSWORD_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "password is too long");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "password confirmation mismatch");
        }
        String verifyCodeKey = String.format("email:code:verify:resetPassword:%s", userEmail);
        String correctCode = stringRedisTemplate.opsForValue().get(verifyCodeKey);
        if (StrUtil.isBlank(correctCode) || !correctCode.equals(emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid or expired verify code");
        }
        User targetUser = this.mapper.selectOneByQuery(new QueryWrapper().eq(User::getUserEmail, userEmail));
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "user not found");
        }
        targetUser.setLoginPassword(passwordEncoder.encode(newPassword));
        targetUser.setUpdateTime(LocalDateTime.now());
        boolean updateResult = this.updateById(targetUser);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "failed to reset password");
        }
        tokenStoreManager.deleteAllToken(String.valueOf(targetUser.getUserStatus()), targetUser.getUserId());
        log.info("user reset password success : {}", targetUser.getUserId());
        stringRedisTemplate.delete(verifyCodeKey);
        return true;
    }

    @Override
    public String updateUserAvatar(MultipartFile multipartFile) {
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = this.mapper.selectOneById(userId);
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtil.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, "avatar");
        loginUser.setUserAvatar(uploadPictureResult.getPicUrl());
        boolean result = mapper.update(loginUser) > 0;
        log.info("user change avatar success : {}", userId);
        ThrowUtil.throwIf(!result, ErrorCode.OPERATION_ERROR, "修改头像失败");
        return uploadPictureResult.getPicUrl();
    }

    @Override
    public boolean banOrUnbanUser(String userId, Boolean isUnban, User admin) {
        if (userId == null || isUnban == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (admin == null || !UserConstant.ADMIN_ROLE.equals(admin.getUserRole())) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "仅管理员可操作");
        }
        User targetUser = this.getById(userId);
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        ThrowUtil.throwIf(admin.getUserId().equals(userId), ErrorCode.OPERATION_ERROR, "不能对自己执行封禁或解封");
        ThrowUtil.throwIf(UserConstant.ADMIN_ROLE.equals(targetUser.getUserRole()), ErrorCode.OPERATION_ERROR, "不能封禁管理员");
        boolean isBanned = Objects.equals(targetUser.getUserStatus(), UserStatusEnum.BANNED.getValue())
                || CrawlerConstant.BAN_ROLE.equals(targetUser.getUserRole());
        ThrowUtil.throwIf(isUnban == !isBanned, ErrorCode.OPERATION_ERROR, isUnban ? "该用户当前未被封禁" : "该用户当前已被封禁");

        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setUserStatus(isUnban ? UserStatusEnum.NORMAL.getValue() : UserStatusEnum.BANNED.getValue());
        if (isUnban && CrawlerConstant.BAN_ROLE.equals(targetUser.getUserRole())) {
            updateUser.setUserRole(UserConstant.DEFAULT_ROLE);
        }
        updateUser.setUpdateTime(LocalDateTime.now());
        boolean result = this.updateById(updateUser);
        if (!result) {
            return false;
        }
        tokenStoreManager.deleteAllToken(String.valueOf(targetUser.getUserStatus()), userId);
        String banKey = String.format("user:ban:%s", userId);
        if (isUnban) {
            stringRedisTemplate.delete(banKey);
        } else {
            stringRedisTemplate.opsForValue().set(banKey, "1");
        }
        log.info("admin [{}] {} user [{}]", admin.getUserEmail(), isUnban ? "unban" : "ban", targetUser.getUserEmail());
        return true;
    }

    @Override
    public boolean updateUserRole(String userId, String userRole, User admin) {
        ThrowUtil.throwIf(StrUtil.hasBlank(userId, userRole), ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtil.throwIf(admin == null || !UserConstant.ADMIN_ROLE.equals(admin.getUserRole()), ErrorCode.NOT_AUTH_ERROR, "仅管理员可操作");
        ThrowUtil.throwIf(admin.getUserId().equals(userId), ErrorCode.OPERATION_ERROR, "不能修改自己的角色");
        ThrowUtil.throwIf(UserRoleEnum.getEnumByValue(userRole) == null, ErrorCode.PARAMS_ERROR, "非法用户角色");
        User targetUser = this.getById(userId);
        ThrowUtil.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtil.throwIf(Objects.equals(targetUser.getUserStatus(), UserStatusEnum.BANNED.getValue()), ErrorCode.OPERATION_ERROR, "封禁用户请先解封后再修改角色");
        if (userRole.equals(targetUser.getUserRole())) {
            return true;
        }

        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setUserRole(userRole);
        updateUser.setUpdateTime(LocalDateTime.now());
        boolean result = this.updateById(updateUser);
        if (result) {
            tokenStoreManager.deleteAllToken(String.valueOf(targetUser.getUserStatus()), userId);
            log.info("admin [{}] update user [{}] role from [{}] to [{}]",
                    admin.getUserEmail(), targetUser.getUserEmail(), targetUser.getUserRole(), userRole);
        }
        return result;
    }

    @Override
    public UserManageStatsVO getUserManageStats() {
        long totalUserCount = this.count(QueryWrapper.create().eq("is_deleted", LOGIC_DELETED_NO));
        long adminUserCount = this.count(QueryWrapper.create()
                .eq("is_deleted", LOGIC_DELETED_NO)
                .eq("user_role", UserConstant.ADMIN_ROLE));
        long bannedUserCount = this.count(QueryWrapper.create()
                .eq("is_deleted", LOGIC_DELETED_NO)
                .eq("user_status", UserStatusEnum.BANNED.getValue()));
        long normalUserCount = Math.max(totalUserCount - adminUserCount - bannedUserCount, 0L);
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime sevenDayStart = todayStart.minusDays(6);
        long todayRegisterCount = this.count(QueryWrapper.create()
                .eq("is_deleted", LOGIC_DELETED_NO)
                .ge("create_time", todayStart));
        long recentSevenDayRegisterCount = this.count(QueryWrapper.create()
                .eq("is_deleted", LOGIC_DELETED_NO)
                .ge("create_time", sevenDayStart));

        UserManageStatsVO statsVO = new UserManageStatsVO();
        statsVO.setTotalUserCount(totalUserCount);
        statsVO.setNormalUserCount(normalUserCount);
        statsVO.setAdminUserCount(adminUserCount);
        statsVO.setBannedUserCount(bannedUserCount);
        statsVO.setTodayRegisterCount(todayRegisterCount);
        statsVO.setRecentSevenDayRegisterCount(recentSevenDayRegisterCount);
        return statsVO;
    }
}
