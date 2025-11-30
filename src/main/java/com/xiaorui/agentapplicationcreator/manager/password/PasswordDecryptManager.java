package com.xiaorui.agentapplicationcreator.manager.password;

import cn.hutool.crypto.symmetric.AES;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;

/**
 * @description: 密码解密管理（前端在传输时使用AES加密，后端先使用AES解密，再进行处理（暂时作废））（暂时先是前端使用明文传输，后端使用BCrypt加密）
 * @author: xiaorui
 * @date: 2025-11-30 14:35
 **/
//@Component
public class PasswordDecryptManager {

    //private static final Logger logger = LoggerFactory.getLogger(PasswordDecryptManager.class);
    /**
     * 用于aes签名的key，16位
     */
    @Value("${auth.password.signKey:-xiaorui-password}")
    public String passwordSignKey;

    /**
     * 解密密码
     */
    public String decryptPassword(String data) {
        // 在使用oracle的JDK时，JAR包必须签署特殊的证书才能使用。
        // 解决方案 1.使用openJDK或者非oracle的JDK（建议） 2.添加证书
        // hutool的aes报错可以打开下面那段代码
        // SecureUtil.disableBouncyCastle();
        AES aes = new AES(passwordSignKey.getBytes(StandardCharsets.UTF_8));
        String decryptStr;
        String decryptPassword;
        try {
            decryptStr = aes.decryptStr(data);
            decryptPassword = decryptStr.substring(13);
        } catch (Exception e) {
            //logger.error("Exception:", e);
            throw new BusinessException("AES 解密错误", e);

        }
        return decryptPassword;
    }

}