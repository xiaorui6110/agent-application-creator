package com.xiaorui.agentapplicationcreator.agent.config;


import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.state.AgentStateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 智能体状态配置（RedisSaver）
 * @author: xiaorui
 * @date: 2025-12-30 13:51
 **/
@Configuration
public class AgentStateConfig {

    @Bean
    public AgentStateFactory<OverAllState> agentStateFactory() {
        // 框架的 OverAllState 本身就支持 Map 构造
        return OverAllState::new;
    }
}

