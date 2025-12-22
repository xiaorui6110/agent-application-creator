package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.service.IService;
import com.mybatisflex.core.query.QueryWrapper;
import com.xiaorui.agentapplicationcreator.model.dto.user.UserQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.TokenInfoVO;
import com.xiaorui.agentapplicationcreator.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户表 服务层。TODO 一些管理员的方法还未实现，比如 getUserList、等等
 *
 * @author xiaorui
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册（使用邮箱进行注册）
     *
     * @param userEmail 用户邮箱
     * @param loginPassword  登录密码
     * @param checkPassword  确认密码
     * @param emailVerifyCode  邮箱验证码
     * @return 用户id
     */
    String userRegister(String userEmail,  String loginPassword, String checkPassword, String emailVerifyCode);

    /**
     * 用户登录（邮箱、密码登录）
     *
     * @param userEmail  用户邮箱
     * @param loginPassword   登录密码
     * @return 用户信息vo
     */
    TokenInfoVO userLogin(String userEmail, String loginPassword);

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱
     * @param type 验证类型
     * @param request HTTP请求
     */
    void sendEmailCode(String email, String type, HttpServletRequest request);

    /**
     * 获取图形验证码
     *
     * @return 图形验证码
     */
    Map<String, String> getPictureVerifyCode();

    /**
     * 校验图形验证码（从登录逻辑中抽离出来）
     *
     * @param verifyCode 用户输入的验证码
     * @param serverVerifyCode 服务器存储的验证码
     * @return 是否正确
     */
    boolean checkPictureVerifyCode(String verifyCode, String serverVerifyCode);

    /**
     * 获取查询条件（通过userId和nickName查询）
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);


    /**
     * 用户登出（退出登录）
     *
     * @param request HTTP请求
     * @return 是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取用户信息
     *
     * @return 用户信息vo
     */
    UserVO getUserInfo();

    /**
     * 修改用户邮箱
     *
     * @param newUserEmail 新邮箱
     * @param emailVerifyCode 邮箱验证码
     * @return 是否成功
     */
    boolean changeUserEmail(String newUserEmail, String emailVerifyCode);

    /**
     * 修改用户密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @return 是否成功
     */
    boolean changeUserPassword(String oldPassword, String newPassword , String checkPassword);

    /**
     * 重置用户密码
     *
     * @param userEmail 用户邮箱
     * @param emailVerifyCode 邮箱验证码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @return 是否成功
     */
    boolean resetUserPassword(String userEmail, String emailVerifyCode, String newPassword, String checkPassword);

    /**
     * 修改用户头像（上传本地文件修改头像）
     *
     * @param multipartFile 文件
     * @return 上传图片的 URL
     */
    String updateUserAvatar(MultipartFile multipartFile);





}
