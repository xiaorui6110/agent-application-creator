package com.xiaorui.agentapplicationcreator.ai.memory;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.serializer.StateSerializer;
import com.alibaba.cloud.ai.graph.serializer.plain_text.jackson.SpringAIJacksonStateSerializer;
import com.alibaba.cloud.ai.graph.state.AgentStateFactory;
import org.redisson.api.RedissonClient;

/**
 * @description: 自定义 RedisSaver
 * @author: xiaorui
 * @date: 2025-12-29 16:19
 **/
public class CustomRedisSaver extends RedisSaver {

    public CustomRedisSaver(RedissonClient redissonClient, StateSerializer stateSerializer) {
        super(redissonClient, stateSerializer);
    }

    public static RedisSaver create(RedissonClient redissonClient, AgentStateFactory<OverAllState> stateFactory) {
        StateSerializer serializer = new SpringAIJacksonStateSerializer(stateFactory);
        return new CustomRedisSaver(redissonClient, serializer);
    }
}