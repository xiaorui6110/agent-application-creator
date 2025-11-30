package com.xiaorui.agentapplicationcreator.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.xiaorui.agentapplicationcreator.constants.OauthCacheConstant;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.manager.token.TokenStoreManager;
import com.xiaorui.agentapplicationcreator.model.bo.UserInfoBO;
import com.xiaorui.agentapplicationcreator.model.bo.UserInfoInTokenBO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @description: Spring Security 工具类（从上下文中获取用户信息）
 * @author: xiaorui
 * @date: 2025-11-30 14:23
 **/
@Slf4j
@UtilityClass
public class SecurityUtil {

    private static StringRedisTemplate stringRedisTemplate;

    private static TokenStoreManager tokenStoreManager;

    static {
        // 通过 ApplicationContext 获取 Bean
        ApplicationContext context = SpringContextUtil.getApplicationContext();
        stringRedisTemplate = context.getBean(StringRedisTemplate.class);
        tokenStoreManager = context.getBean(TokenStoreManager.class);
    }

    /**
     * 获取用户
     */
    public static UserInfoBO getUser() {
        String token = StpUtil.getTokenValue();
        String keyName = OauthCacheConstant.USER_INFO + token;
        // 从redis中获取token
        String userInfoInTokenBOInRedis = stringRedisTemplate.opsForValue().get(keyName);
        // 缓存的内容为："{\"nickName\":\"xiaorui\",\"userId\":\"352650299392131072\",\"userRole\":\"admin\",\"userStatus\":1}"
        try {
            if (StrUtil.isNotEmpty(userInfoInTokenBOInRedis)) {
                // 去除外层多余的双引号和转义字符
                userInfoInTokenBOInRedis = userInfoInTokenBOInRedis.replace("\\\"", "\"");
                if (userInfoInTokenBOInRedis.startsWith("\"") && userInfoInTokenBOInRedis.endsWith("\"")) {
                    userInfoInTokenBOInRedis = userInfoInTokenBOInRedis.substring(1, userInfoInTokenBOInRedis.length() - 1);
                }
            }
        } catch (JSONException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息解析失败");
        }
        // 将缓存中的内容转换为UserInfoInTokenBO对象
        UserInfoInTokenBO userInfoInTokenBO = JSON.parseObject(userInfoInTokenBOInRedis, UserInfoInTokenBO.class);
        // 解析结果 userInfoInTokenBO: UserInfoInTokenBO(userId=352650299392131072, nickName=xiaorui, userRole=admin, userStatus=1, otherId=null)
        // 添加空值检查
        if (userInfoInTokenBO == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录或登录已过期");
        }
        UserInfoBO userInfoBO = new UserInfoBO();
        userInfoBO.setUserId(userInfoInTokenBO.getUserId());

        return userInfoBO;
    }
}
