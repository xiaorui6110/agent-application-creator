package com.xiaorui.agentapplicationcreator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @description: 邮件发送工具类
 * @author: xiaorui
 * @date: 2025-11-30 14:27
 **/
@Component
public class EmailSenderUtil {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(EmailSenderUtil.class);

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    /**
     * 生成验证码并发送邮件
     *
     * @param toEmail  邮箱地址
     * @param generatedCode 验证码
     */
    public void sendEmail(String toEmail, String generatedCode) {
        // 配置邮件服务器属性
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", String.valueOf(port));
        // 禁用STARTTLS
        properties.put("mail.smtp.starttls.enable", "false");
        // 启用SSL加密连接
        properties.put("mail.smtp.ssl.enable", "true");
        // 使用SSL套接字工厂
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        // SSL端口
        properties.put("mail.smtp.socketFactory.port", String.valueOf(port));
        // 需要身份验证
        properties.put("mail.smtp.auth", "true");
        // 创建带有身份验证的邮件会话
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // 提供发件人邮箱和密码进行身份验证
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            // 创建邮件消息
            MimeMessage message = new MimeMessage(session);
            // 设置发件人 from
            message.setFrom(new InternetAddress(from));
            // 设置收件人 to
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            // 编码邮件主题，防止中文乱码，"B"表示Base64编码，适用于包含非ASCII字符的文本
            String encodedSubject = MimeUtility.encodeText("xiaorui-mail 邮箱验证码", "UTF-8", "B");
            message.setSubject(encodedSubject, "UTF-8");
            // 读取HTML模板并替换验证码
            String htmlContent = readHTMLFromFile();
            // 替换模板中的占位符为实际验证码
            htmlContent = htmlContent.replace(":data=\"verify\"", ":data=\"" + generatedCode + "\"").replace("000000", generatedCode);
            // 设置邮件内容为HTML格式，并指定字符编码
            message.setContent(htmlContent, "text/html;charset=UTF-8");
            // 发送邮件
            Transport.send(message);
            logger.info("Sent message successfully to {}", toEmail);
        } catch (MessagingException | IOException e) {
            // 记录发送失败的错误信息
            logger.error("Error sending email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * 读取邮件模板
     */
    private String readHTMLFromFile() throws IOException {
        // 从类路径资源中加载HTML模板文件
        ClassPathResource resource = new ClassPathResource("static/html/verifycode_email.html");
        // 使用try-with-resources确保资源正确关闭
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            // 逐行读取HTML内容
            while ((line = reader.readLine())!= null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    /**
     * 发送爬虫警告邮件
     *
     * @param toEmail 收件人邮箱
     * @param htmlContent html内容
     */
    public void sendWarningEmail(String toEmail, String htmlContent) {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", String.valueOf(port));
        properties.put("mail.smtp.starttls.enable", "false");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.port", String.valueOf(port));
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            // 编码邮件主题
            String encodedSubject = MimeUtility.encodeText("爬虫警告通知", "UTF-8", "B");
            message.setSubject(encodedSubject, "UTF-8");
            // 设置邮件内容
            message.setContent(htmlContent, "text/html;charset=UTF-8");
            Transport.send(message);
            logger.info("邮件发送成功");
        } catch (MessagingException | IOException e) {
            logger.error("邮件发送失败: {}", e.getMessage(), e);
        }
    }
}