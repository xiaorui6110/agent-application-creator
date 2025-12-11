package com.xiaorui.agentapplicationcreator.security.adapter;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @description: 自定义路径
 * @author: xiaorui
 * @date: 2025-11-30 14:42
 **/
@Component
public class ResourceServerAdapter extends DefaultAuthConfigAdapter {

    @Override
    public List<String> pathPatterns() {
        return Collections.singletonList("/api/*");
    }
}
