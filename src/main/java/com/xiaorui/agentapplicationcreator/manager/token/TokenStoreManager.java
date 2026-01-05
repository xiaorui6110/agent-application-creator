package com.xiaorui.agentapplicationcreator.manager.token;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.xiaorui.agentapplicationcreator.constant.OauthCacheConstant;
import com.xiaorui.agentapplicationcreator.enums.SysTypeEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.model.bo.TokenInfoBO;
import com.xiaorui.agentapplicationcreator.model.bo.UserInfoInTokenBO;
import com.xiaorui.agentapplicationcreator.model.vo.TokenInfoVO;
import com.xiaorui.agentapplicationcreator.response.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.xiaorui.agentapplicationcreator.enums.UserStatusEnum.BANNED;

/**
 * @description: token管理： 1. 登陆返回token 2. 刷新token 3. 清除用户过去token 4. 校验token
 * @author: xiaorui
 * @date: 2025-11-30 14:16
 **/
@Slf4j
@Component
public class TokenStoreManager {

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenStoreManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        log.info("RedisTemplate injected successfully: {}", redisTemplate != null);
    }

    /**
     * 以Sa-Token技术生成token，并返回token信息
     * @param userInfoInTokenBO 用户信息
     * @return token信息
     */
    public TokenInfoBO storeAccessSaToken(UserInfoInTokenBO userInfoInTokenBO) {
        // token生成
        String uid = this.getUid(userInfoInTokenBO.getUserStatus().toString(), userInfoInTokenBO.getUserId());
        int timeoutSecond = getExpiresTime(userInfoInTokenBO.getUserStatus());
        StpUtil.login(uid, timeoutSecond);
        String token = StpUtil.getTokenValue();
        // 用户信息存入缓存
        String keyName = OauthCacheConstant.USER_INFO + token;
        redisTemplate.delete(keyName);
        redisTemplate.opsForValue().set(
                keyName,
                JSON.toJSONString(userInfoInTokenBO),
                timeoutSecond,
                TimeUnit.SECONDS
        );
        // 数据封装返回(token不用加密)
        TokenInfoBO tokenInfoBO = new TokenInfoBO();
        tokenInfoBO.setUserInfoInToken(userInfoInTokenBO);
        tokenInfoBO.setExpiresTime(timeoutSecond);
        tokenInfoBO.setAccessToken(token);
        tokenInfoBO.setRefreshToken(token);
        return tokenInfoBO;
    }

    /**
     * 计算过期时间（单位:秒）
     * @param sysType 系统类型
     * @return 过期时间
     */
    private int getExpiresTime(int sysType) {
        // 3600秒
        int expiresIn = 3600;
        // 普通用户token过期时间
        if (Objects.equals(sysType, SysTypeEnum.ORDINARY.getValue())) {
            expiresIn = expiresIn * 24 * 30;
        }
        // 系统管理员的token过期时间
        if (Objects.equals(sysType, SysTypeEnum.ADMIN.getValue())) {
            expiresIn = expiresIn * 24 * 30;
        }
        return expiresIn;
    }

    /**
     * 根据accessToken 获取用户信息
     * @param accessToken accessToken
     * @param needDecrypt 是否需要解密
     * @return 用户信息
     */
    public UserInfoInTokenBO getUserInfoByAccessToken(String accessToken, boolean needDecrypt) {
        if (StrUtil.isBlank(accessToken)) {
            throw new BusinessException(ResponseEnum.UNAUTHORIZED,"accessToken is blank");
        }
        String keyName = OauthCacheConstant.USER_INFO + accessToken;
        Object redisCache = redisTemplate.opsForValue().get(keyName);
        if (ObjectUtil.isNull(redisCache)) {
            throw new BusinessException(ResponseEnum.UNAUTHORIZED,"登录过期，请重新登录");
        }
        return JSON.parseObject(redisCache.toString(), UserInfoInTokenBO.class);
    }

    /**
     * 刷新token，并返回新的token
     * @param refreshToken 刷新token
     * @return 新的token信息
     */
    public TokenInfoBO refreshToken(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            throw new BusinessException(ResponseEnum.UNAUTHORIZED,"refreshToken is blank");
        }
        // 删除旧token
        UserInfoInTokenBO userInfoInTokenBO = getUserInfoByAccessToken(refreshToken, false);
        this.deleteCurrentToken(refreshToken);
        // 保存一份新的token
        return storeAccessSaToken(userInfoInTokenBO);
    }

    /**
     * 删除指定用户的全部的token
     */
    public void deleteAllToken(String sysType, String userId) {
        // 删除用户缓存
        String uid = this.getUid(sysType, userId);
        List<String> tokens = StpUtil.getTokenValueListByLoginId(uid);
        if (!CollectionUtils.isEmpty(tokens)) {
            List<String> keyNames = new ArrayList<>();
            for (String token : tokens) {
                keyNames.add(OauthCacheConstant.USER_INFO + token);
            }
            redisTemplate.delete(keyNames);
        }
        // 移除token
        StpUtil.logout(userId);
    }

    /**
     * 生成token，并返回token展示信息
     * @param userInfoInTokenBO 用户信息
     * @return token展示信息
     */
    public TokenInfoVO storeAndGetVo(UserInfoInTokenBO userInfoInTokenBO) {
        if (userInfoInTokenBO.getUserStatus().equals(BANNED.getValue())){
            // 用户已禁用，请联系管理员
            throw new BusinessException("用户已禁用，请联系管理员");
        }
        TokenInfoBO tokenInfoBO = storeAccessSaToken(userInfoInTokenBO);
        // 数据封装返回
        TokenInfoVO tokenInfoVO = new TokenInfoVO();
        tokenInfoVO.setAccessToken(tokenInfoBO.getAccessToken());
        tokenInfoVO.setRefreshToken(tokenInfoBO.getRefreshToken());
        tokenInfoVO.setExpiresTime(tokenInfoBO.getExpiresTime());
        return tokenInfoVO;
    }

    /**
     * 删除当前登录的token
     * @param accessToken 令牌
     */
    public void deleteCurrentToken(String accessToken) {
        // 删除用户缓存
        String keyName = OauthCacheConstant.USER_INFO + accessToken;
        redisTemplate.delete(keyName);
        // 移除token
        StpUtil.logoutByTokenValue(accessToken);
    }

    /**
     * 生成各系统唯一uid
     * @param sysType 系统类型
     * @param userId 用户id
     * @return uid
     */
    private String getUid(String sysType, String userId) {
        return sysType + ":" + userId;
    }

}
