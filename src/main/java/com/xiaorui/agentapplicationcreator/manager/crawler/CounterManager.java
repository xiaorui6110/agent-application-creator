package com.xiaorui.agentapplicationcreator.manager.crawler;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.IntegerCodec;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @description: 通用计数器（使用 Redis ZSet 实现，可用于实现频率统计、限流、封禁等等）
 * @author: xiaorui
 * @date: 2025-12-31 16:56
 **/
@Slf4j
@Service
public class CounterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 默认滑动窗口时间：1分钟
     */
    private static final long DEFAULT_WINDOW_SECONDS = 60;
    /**
     *  默认阈值：10次
     */
    private static final long DEFAULT_THRESHOLD = 10;

    /**
     * 滑动窗口计数（默认1分钟窗口，阈值10）
     *
     * @param key 缓存键（如：api:access:user123）
     * @return 窗口内的总访问数（若≥阈值，返回-1标识触发限制）
     */
    public long slidingWindowIncrAndGet(String key) {
        return slidingWindowIncrAndGet(key, DEFAULT_WINDOW_SECONDS, DEFAULT_THRESHOLD);
    }

    /**
     * 滑动窗口计数（自定义窗口和阈值）
     *
     * @param key 缓存键（用户/接口维度）
     * @param windowSeconds 滑动窗口时长（秒）
     * @param threshold 访问阈值（超过则触发限制）
     * @return 窗口内总访问数（-1 = 触发限制）
     */
    public long slidingWindowIncrAndGet(String key, long windowSeconds, long threshold) {
        if (StrUtil.isBlank(key)) {
            return 0;
        }

        // 1. 计算窗口边界：当前时间 - 窗口时长（过期时间戳）
        long currentTimestamp = Instant.now().getEpochSecond();
        long expireTimestamp = currentTimestamp - windowSeconds;
        // 2. 生成唯一标识（避免ZSet的value重复，用UUID）
        String uniqueValue = UUID.randomUUID().toString();

        // 3. Lua脚本：原子执行「删过期数据 → 统计总数 → 判断阈值 → 新增记录」
        // 核心逻辑：
        // - ZREMRangeByScore：删除窗口外的过期记录
        // - ZCARD：统计当前窗口内的总数
        // - 若总数≥阈值，返回-1；否则ZADD新增记录，设置key过期时间，返回总数+1
        String luaScript =
                "-- 1. 删除滑动窗口外的过期记录\n" +
                        "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1]);\n" +
                        "-- 2. 统计当前窗口内的访问数\n" +
                        "local count = redis.call('ZCARD', KEYS[1]);\n" +
                        "-- 3. 判断是否超过阈值\n" +
                        "if count >= tonumber(ARGV[2]) then\n" +
                        "    return -1;\n" +
                        "end\n" +
                        "-- 4. 新增本次访问的时间戳（ZADD：score=时间戳，value=唯一标识）\n" +
                        "redis.call('ZADD', KEYS[1], ARGV[3], ARGV[4]);\n" +
                        "-- 5. 设置Key的过期时间（避免内存泄漏，过期时间=窗口时长+10秒）\n" +
                        "redis.call('EXPIRE', KEYS[1], tonumber(ARGV[5]));\n" +
                        "-- 6. 返回最新计数\n" +
                        "return count + 1;";

        try {
            RScript script = redissonClient.getScript(IntegerCodec.INSTANCE);
            // 执行Lua脚本，参数说明：
            // KEYS[1] = redisKey
            // ARGV[1] = 过期时间戳（expireTimestamp）
            // ARGV[2] = 阈值（threshold）
            // ARGV[3] = 当前时间戳（currentTimestamp）
            // ARGV[4] = 唯一标识（uniqueValue）
            // ARGV[5] = key过期时间（windowSeconds + 10，留冗余）
            Object resultObj = script.eval(
                    RScript.Mode.READ_WRITE,
                    luaScript,
                    RScript.ReturnType.INTEGER,
                    Collections.singletonList(key),
                    expireTimestamp,
                    threshold,
                    currentTimestamp,
                    uniqueValue,
                    windowSeconds + 10
            );
            return (long) resultObj;
        } catch (Exception e) {
            log.error("滑动窗口计数异常，key:{}", key, e);
            return 0;
        }
    }

    public long incrAndGetCounter(String key) {
        return incrAndGetCounter(key, 1, TimeUnit.MINUTES);
    }

    public long incrAndGetCounter(String key, int timeInterval, TimeUnit timeUnit) {
        int expirationTimeInSeconds;
        switch (timeUnit) {
            case SECONDS:
                expirationTimeInSeconds = timeInterval;
                break;
            case MINUTES:
                expirationTimeInSeconds = timeInterval * 60;
                break;
            case HOURS:
                expirationTimeInSeconds = timeInterval * 60 * 60;
                break;
            default:
                throw new IllegalArgumentException("Unsupported TimeUnit. Use SECONDS, MINUTES, or HOURS.");
        }
        return incrAndGetCounter(key, timeInterval, timeUnit, expirationTimeInSeconds);
    }

    public long incrAndGetCounter(String key, int timeInterval, TimeUnit timeUnit, long expirationTimeInSeconds) {
        if (StrUtil.isBlank(key)) {
            return 0;
        }
        long timeFactor;
        switch (timeUnit) {
            case SECONDS:
                timeFactor = Instant.now().getEpochSecond() / timeInterval;
                break;
            case MINUTES:
                timeFactor = Instant.now().getEpochSecond() / timeInterval / 60;
                break;
            case HOURS:
                timeFactor = Instant.now().getEpochSecond() / timeInterval / 3600;
                break;
            default:
                throw new IllegalArgumentException("不支持的单位");
        }

        String redisKey = key + ":" + timeFactor;
        String luaScript =
                "if redis.call('exists', KEYS[1]) == 1 then " +
                        "  return redis.call('incr', KEYS[1]); " +
                        "else " +
                        "  redis.call('set', KEYS[1], 1); " +
                        "  redis.call('expire', KEYS[1], ARGV[1]); " +
                        "  return 1; " +
                        "end";

        RScript script = redissonClient.getScript(IntegerCodec.INSTANCE);
        Object countObj = script.eval(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.INTEGER,
                Collections.singletonList(redisKey), expirationTimeInSeconds
        );
        return (long) countObj;
    }
}
