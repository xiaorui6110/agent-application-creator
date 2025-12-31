package com.xiaorui.agentapplicationcreator.util;

import com.jcraft.jsch.*;
import com.xiaorui.agentapplicationcreator.config.SftpConfig;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @description: SFTP文件上传工具类（用于将本地文件上传到Linux服务器）
 * @author: xiaorui
 * @date: 2025-12-26 13:49
 **/
@Component
public class SftpFileUtil {

    private static final Logger logger = LoggerFactory.getLogger(SftpFileUtil.class);

    /**
     * SFTP连接对象
     */
    private ChannelSftp sftp = null;
    /**
     * SSH会话对象
     */
    private Session session = null;

    @Resource
    private SftpConfig sftpConfig;


    /**
     * 静态方法：上传单个文件
     */
    public void uploadToLinux(String localFilePath, String remoteDir) throws Exception {

        try {
            connect();
            uploadFile(localFilePath, remoteDir);
        } finally {
            disconnect();
        }
    }

    /**
     * 静态方法：上传整个文件夹（实际使用）
     */
    public void uploadDirToLinux(String localDir, String remoteParentDir) throws Exception {
        try {
            connect();
            uploadDirectory(localDir, remoteParentDir);
        } finally {
            disconnect();
        }
    }


    /**
     * 连接SFTP服务器
     */
    public void connect() throws JSchException {
        // 从配置文件中获取
        String host = sftpConfig.getHost();
        int port = sftpConfig.getPort();
        String username = sftpConfig.getUsername();
        String password = sftpConfig.getPassword();
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setTimeout(5000);
        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        sftp = (ChannelSftp) channel;
        logger.info("SFTP连接成功，服务器：{}", host);
    }


    /**
     * 上传单个文件
     */
    public void uploadFile(String localFilePath, String remoteDir) throws Exception {
        if (localFilePath == null || localFilePath.isEmpty()) {
            throw new IllegalArgumentException("本地文件路径不能为空");
        }
        if (remoteDir == null || remoteDir.isEmpty()) {
            throw new IllegalArgumentException("服务器目标目录不能为空");
        }

        File localFile = new File(localFilePath);
        if (!localFile.exists() || !localFile.isFile()) {
            throw new RuntimeException("本地文件不存在或不是文件：" + localFilePath);
        }

        try (InputStream inputStream = new FileInputStream(localFile)) {
            mkdirs(remoteDir);
            sftp.cd(remoteDir);
            sftp.put(inputStream, localFile.getName(), ChannelSftp.OVERWRITE);
            logger.info("文件上传成功：本地{} -> 服务器{}", localFilePath, remoteDir + localFile.getName());
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            throw e;
        }
    }


    /**
     * 递归上传整个文件夹（包含所有文件和子文件夹）
     *
     * @param localDir 本地文件夹路径（如：D:/WrEIYy）
     * @param remoteParentDir 服务器父目录（如：/home/upload/）
     * @throws Exception 上传异常
     */
    public void uploadDirectory(String localDir, String remoteParentDir) throws Exception {
        // 校验本地文件夹
        File localDirectory = new File(localDir);
        if (!localDirectory.exists() || !localDirectory.isDirectory()) {
            throw new RuntimeException("本地文件夹不存在或不是目录：" + localDir);
        }
        if (remoteParentDir == null || remoteParentDir.isEmpty()) {
            throw new IllegalArgumentException("服务器父目录不能为空");
        }

        // 服务器目标目录 = 父目录 + 本地文件夹名（保持目录名一致）
        String remoteTargetDir = remoteParentDir + localDirectory.getName() + "/";
        // 创建服务器目标目录
        mkdirs(remoteTargetDir);

        // 遍历本地文件夹中的所有文件/子文件夹
        File[] files = localDirectory.listFiles();
        if (files == null || files.length == 0) {
            logger.info("本地文件夹{}为空，无需上传", localDir);
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                // 如果是文件：直接上传到服务器对应目录
                uploadFile(file.getAbsolutePath(), remoteTargetDir);
            } else if (file.isDirectory()) {
                // 如果是子文件夹：递归上传
                uploadDirectory(file.getAbsolutePath(), remoteTargetDir);
            }
        }
        logger.info("文件夹上传完成：本地{} -> 服务器{}", localDir, remoteTargetDir);
    }

    /**
     * 递归创建Linux服务器目录
     */
    private void mkdirs(String dir) throws SftpException {
        String[] dirs = dir.split("/");
        StringBuilder path = new StringBuilder();
        for (String d : dirs) {
            if (d.isEmpty()) {
                continue;
            }
            path.append("/").append(d);
            try {
                sftp.cd(path.toString());
            } catch (SftpException e) {
                sftp.mkdir(path.toString());
                sftp.cd(path.toString());
            }
        }
    }

    /**
     * 关闭SFTP连接
     */
    public void disconnect() {
        if (sftp != null && sftp.isConnected()) {
            sftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        logger.info("SFTP连接已关闭");
    }

}