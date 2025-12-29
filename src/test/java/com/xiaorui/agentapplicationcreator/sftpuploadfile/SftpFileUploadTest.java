package com.xiaorui.agentapplicationcreator.sftpuploadfile;

import com.xiaorui.agentapplicationcreator.util.SftpFileUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description: SFTP文件上传测试
 * @author: xiaorui
 * @date: 2025-12-26 13:54
 **/
@Slf4j
@SpringBootTest
public class SftpFileUploadTest {

    @Resource
    private SftpFileUtil sftpFileUtil;

    /**
     * 测试单文件上传
     */
    @Test
    public void testSftpUploadFile() throws Exception {

        String localFilePath = "D:\\JAVA1021\\GraduationProject\\Code\\agent-application-creator\\tmp\\code_output\\app_id12121212\\pomodoro-timer.html";
        String remoteDir = "/home/shenrui/nginx/code_deploy/";
        sftpFileUtil.uploadToLinux(localFilePath, remoteDir);

    }

    /**
     * 测试整个文件夹文件上传
     */
    @Test
    public void testSftpUploadDir() throws Exception {

        String localFilePath = "D:\\JAVA1021\\GraduationProject\\Code\\agent-application-creator\\tmp\\code_output\\app_id12121212";
        String remoteDir = "/home/shenrui/nginx/code_deploy/";
        sftpFileUtil.uploadDirToLinux(localFilePath, remoteDir);

    }

}
