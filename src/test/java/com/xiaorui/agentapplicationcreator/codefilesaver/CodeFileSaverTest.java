package com.xiaorui.agentapplicationcreator.codefilesaver;

import com.xiaorui.agentapplicationcreator.util.CodeFileSaverUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 代码文件保存器测试类
 * @author: xiaorui
 * @date: 2025-12-24 16:23
 **/
@SpringBootTest
public class CodeFileSaverTest {

    @Resource
    private CodeFileSaverUtil codeFileSaverUtil;

    @Test
    public void testCodeFileSaver() throws IOException {
        Map<String, String> map = new HashMap<>();
        // 添加假数据（覆盖）
        map.put("test", "test123456");
        map.put("test1.html", "test111");
        map.put("test2.css", "test222");
        map.put("test3.js", "test333");
        String appId = "123456";
        codeFileSaverUtil.writeFilesToLocal(map, appId);
    }

}
