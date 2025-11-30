package com.xiaorui.agentapplicationcreator.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @description: 默认的授权配置适配器，用于获取需要排除授权的路径配置
 * @author: xiaorui
 * @date: 2025-11-30 14:42
 **/

public class DefaultAuthConfigAdapter implements AuthConfigAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAuthConfigAdapter.class);

    public DefaultAuthConfigAdapter() {
        logger.info("not implement other AuthConfigAdapter, use DefaultAuthConfigAdapter... all url need auth...");
    }

    @Override
    public List<String> pathPatterns() {
        return Collections.singletonList("/*");
    }

    @Override
    public List<String> excludePathPatterns() {
        return Collections.emptyList();
    }
}