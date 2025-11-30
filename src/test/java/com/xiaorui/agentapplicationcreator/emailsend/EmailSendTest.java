package com.xiaorui.agentapplicationcreator.emailsend;

import com.xiaorui.agentapplicationcreator.utils.EmailSenderUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description: 邮件发送测试
 * @author: xiaorui
 * @date: 2025-11-30 14:31
 **/
@SpringBootTest
public class EmailSendTest {

    @Resource
    private EmailSenderUtil emailSenderUtil;

    // 测试邮箱（替换为实际可接收邮件的地址）
    private static final String TEST_TO_EMAIL = "368649957@qq.com";

    /**
     * 测试发送验证码邮件
     */
    @Test
    public void testSendVerificationEmail() {
        // 可随机生成或固定测试值
        String testCode = "123456";
        emailSenderUtil.sendEmail(TEST_TO_EMAIL, testCode);
    }

}