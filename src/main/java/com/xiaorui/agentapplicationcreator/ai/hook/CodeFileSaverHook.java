package com.xiaorui.agentapplicationcreator.ai.hook;

import com.xiaorui.agentapplicationcreator.ai.context.AgentContext;
import com.xiaorui.agentapplicationcreator.ai.model.enums.AgentLifecycleEvent;
import com.xiaorui.agentapplicationcreator.util.CodeFileSaverUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @description: 代码文件保存 Hook
 * @author: xiaorui
 * @date: 2025-12-24 16:10
 **/
@Component
public class CodeFileSaverHook implements AgentHook {

    private final CodeFileSaverUtil codeFileSaverUtil;

    public CodeFileSaverHook(CodeFileSaverUtil codeFileSaverUtil) {
        this.codeFileSaverUtil = codeFileSaverUtil;
    }

    @Override
    public AgentLifecycleEvent event() {
        return AgentLifecycleEvent.AFTER_AGENT;
    }

    @Override
    public void handle(AgentContext context, Map<String, String> files) {

        if (files == null || files.isEmpty()) {
            return;
        }
        String appId = context.getAppId();
        try {
            codeFileSaverUtil.writeFiles(files, appId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}