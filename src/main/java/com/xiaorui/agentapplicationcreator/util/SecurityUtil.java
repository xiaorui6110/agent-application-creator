package com.xiaorui.agentapplicationcreator.util;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.manager.token.TokenStoreManager;
import com.xiaorui.agentapplicationcreator.model.bo.UserInfoBO;
import com.xiaorui.agentapplicationcreator.model.bo.UserInfoInTokenBO;
import lombok.experimental.UtilityClass;
import org.springframework.context.ApplicationContext;

@UtilityClass
public class SecurityUtil {

    private static final TokenStoreManager tokenStoreManager;

    static {
        ApplicationContext context = SpringContextUtil.getApplicationContext();
        tokenStoreManager = context.getBean(TokenStoreManager.class);
    }

    public static UserInfoBO getUserInfo() {
        UserInfoInTokenBO userInfoInToken = AuthUserContextUtil.get();
        if (userInfoInToken == null) {
            String token = StpUtil.getTokenValue();
            if (StrUtil.isNotBlank(token)) {
                userInfoInToken = tokenStoreManager.getUserInfoByAccessToken(token, true);
            }
        }
        if (userInfoInToken == null || StrUtil.isBlank(userInfoInToken.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "user not logged in");
        }
        UserInfoBO userInfoBO = new UserInfoBO();
        userInfoBO.setUserId(userInfoInToken.getUserId());
        userInfoBO.setUserRole(userInfoInToken.getUserRole());
        userInfoBO.setUserStatus(userInfoInToken.getUserStatus());
        return userInfoBO;
    }
}
