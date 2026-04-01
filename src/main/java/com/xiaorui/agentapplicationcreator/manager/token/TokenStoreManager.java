package com.xiaorui.agentapplicationcreator.manager.token;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.xiaorui.agentapplicationcreator.constant.OauthCacheConstant;
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
import java.util.concurrent.TimeUnit;

import static com.xiaorui.agentapplicationcreator.enums.UserStatusEnum.BANNED;

@Slf4j
@Component
public class TokenStoreManager {

    private static final String LOGIN_ID_PREFIX = "user:";
    private static final int TOKEN_EXPIRE_SECONDS = 3600 * 24 * 30;

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenStoreManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        log.info("RedisTemplate injected successfully: {}", redisTemplate != null);
    }

    public TokenInfoBO storeAccessSaToken(UserInfoInTokenBO userInfoInTokenBO) {
        String loginId = buildLoginId(userInfoInTokenBO.getUserId());
        StpUtil.login(loginId, TOKEN_EXPIRE_SECONDS);
        String token = StpUtil.getTokenValue();

        String keyName = OauthCacheConstant.USER_INFO + token;
        redisTemplate.delete(keyName);
        redisTemplate.opsForValue().set(
                keyName,
                JSON.toJSONString(userInfoInTokenBO),
                TOKEN_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );

        TokenInfoBO tokenInfoBO = new TokenInfoBO();
        tokenInfoBO.setUserInfoInToken(userInfoInTokenBO);
        tokenInfoBO.setExpiresTime(TOKEN_EXPIRE_SECONDS);
        tokenInfoBO.setAccessToken(token);
        tokenInfoBO.setRefreshToken(token);
        return tokenInfoBO;
    }

    public UserInfoInTokenBO getUserInfoByAccessToken(String accessToken, boolean needDecrypt) {
        if (StrUtil.isBlank(accessToken)) {
            throw new BusinessException(ResponseEnum.UNAUTHORIZED, "accessToken is blank");
        }
        String keyName = OauthCacheConstant.USER_INFO + accessToken;
        Object redisCache = redisTemplate.opsForValue().get(keyName);
        if (ObjectUtil.isNull(redisCache)) {
            throw new BusinessException(ResponseEnum.UNAUTHORIZED, "login expired");
        }
        return JSON.parseObject(redisCache.toString(), UserInfoInTokenBO.class);
    }

    public TokenInfoBO refreshToken(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            throw new BusinessException(ResponseEnum.UNAUTHORIZED, "refreshToken is blank");
        }
        UserInfoInTokenBO userInfoInTokenBO = getUserInfoByAccessToken(refreshToken, false);
        deleteCurrentToken(refreshToken);
        return storeAccessSaToken(userInfoInTokenBO);
    }

    public void deleteAllToken(String userId) {
        String loginId = buildLoginId(userId);
        List<String> tokens = StpUtil.getTokenValueListByLoginId(loginId);
        if (!CollectionUtils.isEmpty(tokens)) {
            List<String> keyNames = new ArrayList<>();
            for (String token : tokens) {
                keyNames.add(OauthCacheConstant.USER_INFO + token);
            }
            redisTemplate.delete(keyNames);
        }
        StpUtil.logout(loginId);
    }

    public TokenInfoVO storeAndGetVo(UserInfoInTokenBO userInfoInTokenBO) {
        if (userInfoInTokenBO.getUserStatus().equals(BANNED.getValue())) {
            throw new BusinessException("user is banned");
        }
        TokenInfoBO tokenInfoBO = storeAccessSaToken(userInfoInTokenBO);
        TokenInfoVO tokenInfoVO = new TokenInfoVO();
        tokenInfoVO.setAccessToken(tokenInfoBO.getAccessToken());
        tokenInfoVO.setRefreshToken(tokenInfoBO.getRefreshToken());
        tokenInfoVO.setExpiresTime(tokenInfoBO.getExpiresTime());
        return tokenInfoVO;
    }

    public void deleteCurrentToken(String accessToken) {
        String keyName = OauthCacheConstant.USER_INFO + accessToken;
        redisTemplate.delete(keyName);
        StpUtil.logoutByTokenValue(accessToken);
    }

    public String buildLoginId(String userId) {
        if (StrUtil.isBlank(userId)) {
            throw new BusinessException(ResponseEnum.UNAUTHORIZED, "userId is blank");
        }
        return LOGIN_ID_PREFIX + userId.trim();
    }
}
