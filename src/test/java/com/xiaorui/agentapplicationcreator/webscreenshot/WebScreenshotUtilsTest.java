package com.xiaorui.agentapplicationcreator.webscreenshot;

import com.xiaorui.agentapplicationcreator.util.WebScreenshotUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description:
 * @author: xiaorui
 * @date: 2026-01-02 13:25
 **/
@Slf4j
@SpringBootTest
public class WebScreenshotUtilsTest {

    /**
     * 测试网页截图功能（需要梯子）
     */
    @Test
    public void saveWebPageScreenshot() {
        String testUrl = "https://spring.io/";
        String webPageScreenshot = WebScreenshotUtil.saveWebPageScreenshot(testUrl);
        System.out.println(webPageScreenshot);

    }


}
