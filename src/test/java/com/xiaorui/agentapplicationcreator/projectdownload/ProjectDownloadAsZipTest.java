package com.xiaorui.agentapplicationcreator.projectdownload;

import com.xiaorui.agentapplicationcreator.service.ProjectDownloadService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @description: 项目下载测试
 * @author: xiaorui
 * @date: 2026-01-02 16:50
 **/
@SpringBootTest
public class ProjectDownloadAsZipTest {

    @Resource
    private ProjectDownloadService projectDownloadService;

    @Test
    public void testProjectDownload() {
        HttpServletResponse response = new MockHttpServletResponse();
        projectDownloadService.downloadProjectAsZip("tmp/code_output/361683664271351808", "test", response);

    }

}
