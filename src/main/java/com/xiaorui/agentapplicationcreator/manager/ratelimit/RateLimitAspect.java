package com.xiaorui.agentapplicationcreator.manager.ratelimit;

import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * @description: 限流切面（GPT优化）
 * @author: xiaorui
 * @date: 2026-01-21 01:09
 **/
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final String API_KEY_SUFFIX = "api:";
    private static final String USER_KEY_SUFFIX = "user:";
    private static final String IP_KEY_SUFFIX = "ip:";
    private static final String UNKNOWN_IP = "unknown";
    private static final long RATE_LIMIT_KEY_EXPIRE_HOURS = 1L;

    @Resource
    private RedissonClient redissonClient;

    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint point, RateLimit rateLimit) {
        // 1. 生成限流Key
        String limitKey = generateRateLimitKey(point, rateLimit);
        log.debug("开始限流校验，限流Key：{}", limitKey);

        // 2. 获取Redisson限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(limitKey);

        // 3. 设置限流器过期时间（1小时）
        rateLimiter.expire(Duration.ofHours(RATE_LIMIT_KEY_EXPIRE_HOURS));

        // 4. 替换过时方法：使用RateLimiterConfig构建限流规则
        boolean isConfigSet = rateLimiter.trySetRate(
                // 单客户端限流
                RateType.PER_CLIENT,
                // 时间窗口内允许的请求数
                rateLimit.rate(),
                // 时间窗口大小（秒转Duration）
                Duration.ofSeconds(rateLimit.rateInterval())
        );
        // 尝试设置限流规则（已存在则不会覆盖）
        if (!isConfigSet) {
            log.debug("限流规则已存在，无需重复设置，限流Key：{}", limitKey);
        }


        // 5. 尝试获取令牌（1个），获取失败则抛出限流异常
        boolean acquireSuccess = rateLimiter.tryAcquire(1);
        if (!acquireSuccess) {
            log.warn("限流触发，限流Key：{}，限流规则：{}次/{}秒",
                    limitKey, rateLimit.rate(), rateLimit.rateInterval());
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, rateLimit.message());
        }

        log.debug("限流校验通过，限流Key：{}", limitKey);
    }

    /**
     * 生成限流Key
     * @param point 切点
     * @param rateLimit 限流注解
     * @return 唯一限流Key
     */
    private String generateRateLimitKey(JoinPoint point, RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder(RATE_LIMIT_KEY_PREFIX);

        // 添加自定义前缀（非空时）
        String customKey = rateLimit.key().trim();
        if (!customKey.isEmpty()) {
            keyBuilder.append(customKey).append(":");
        }

        // 根据限流类型拼接不同维度的Key
        switch (rateLimit.limitType()) {
            case API:
                appendApiKey(point, keyBuilder);
                break;
            case USER:
                appendUserKey(keyBuilder);
                break;
            case IP:
                appendIpKey(keyBuilder);
                break;
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "不支持的限流类型：" + rateLimit.limitType());
        }

        return keyBuilder.toString();
    }

    /**
     * 拼接API维度的限流Key
     */
    private void appendApiKey(JoinPoint point, StringBuilder keyBuilder) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        // 拼接：类名.方法名（使用全类名更唯一，避免不同包类名重复）
        keyBuilder.append(API_KEY_SUFFIX)
                .append(method.getDeclaringClass().getName())
                .append(".")
                .append(method.getName());
    }

    /**
     * 拼接用户维度的限流Key（未登录则降级为IP）
     */
    private void appendUserKey(StringBuilder keyBuilder) {
        try {
            // 优先获取用户ID
            String userId = SecurityUtil.getUserInfo().getUserId();
            if (userId != null && !userId.trim().isEmpty()) {
                keyBuilder.append(USER_KEY_SUFFIX).append(userId.trim());
                return;
            }
        } catch (BusinessException e) {
            log.debug("用户未登录，降级为IP限流", e);
        } catch (Exception e) {
            log.warn("获取用户信息失败，降级为IP限流", e);
        }
        // 降级为IP限流
        appendIpKey(keyBuilder);
    }

    /**
     * 拼接IP维度的限流Key
     */
    private void appendIpKey(StringBuilder keyBuilder) {
        String clientIp = getClientIP();
        keyBuilder.append(IP_KEY_SUFFIX).append(clientIp);
    }

    /**
     * 获取客户端真实IP（处理代理场景）
     * @return 客户端IP
     */
    private String getClientIP() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("无法获取请求上下文，返回默认IP：{}", UNKNOWN_IP);
            return UNKNOWN_IP;
        }

        HttpServletRequest request = attributes.getRequest();
        String ip = null;

        // 1. 优先从X-Forwarded-For获取（多级代理场景）
        ip = request.getHeader("X-Forwarded-For");
        if (isInvalidIp(ip)) {
            // 2. 其次从X-Real-IP获取
            ip = request.getHeader("X-Real-IP");
            if (isInvalidIp(ip)) {
                // 3. 最后从remoteAddr获取
                ip = request.getRemoteAddr();
            }
        }

        // 处理多级代理，取第一个有效IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 兜底处理
        return isInvalidIp(ip) ? UNKNOWN_IP : ip;
    }

    /**
     * 校验IP是否为无效值
     */
    private boolean isInvalidIp(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip.trim());
    }

}

