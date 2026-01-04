package com.xiaorui.agentapplicationcreator.agent.config;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.state.AgentStateFactory;
import com.xiaorui.agentapplicationcreator.agent.memory.CustomRedisSaver;
import jakarta.annotation.Resource;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: Redis 检查点配置（注入 Bean，解决 RedisSaver 类的构造方法被 protected（受保护）修饰，导致外部包无法调用的问题）
 * @author: xiaorui
 * @date: 2025-12-29 16:28
 **/
@Configuration
public class RedisCheckpointConfig {

    @Resource
    private RedissonClient redissonClient;


    @Resource
    private AgentStateFactory<OverAllState> stateFactory;


    @Bean
    public RedisSaver redisSaver() {
        return CustomRedisSaver.create(redissonClient, stateFactory);
    }
}