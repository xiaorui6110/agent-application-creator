package com.xiaorui.agentapplicationcreator.manager.password;

import cn.hutool.core.util.StrUtil;
import com.xiaorui.agentapplicationcreator.enums.SysTypeEnum;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.util.IpHelperUtil;
import com.xiaorui.agentapplicationcreator.util.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @description: 密码检查管理器（经AI优化代码后）
 * @author: xiaorui
 * @date: 2025-11-30 14:35
 **/
@Component
public class PasswordCheckManager {

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 半小时内最多错误10次
     */
    private static final int TIMES_CHECK_INPUT_PASSWORD_NUM = 10;

    /**
     * 检查用户输入错误的验证码次数
     */
    private static final String CHECK_VALID_CODE_NUM_PREFIX = "checkUserInputErrorPassword_";

    /**
     * 缓存过期时间：30分钟（秒）
     */
    private static final long CACHE_EXPIRE_SECONDS = 1800L;

    /**
     * 检查密码
     *
     * @param sysTypeEnum 系统类型
     * @param userEmail 用户邮箱
     * @param rawPassword 原始密码
     * @param encodedPassword 加密密码
     */
    public void checkPassword(SysTypeEnum sysTypeEnum, String userEmail, String rawPassword, String encodedPassword) {
        // 构建Redis缓存键
        String redisKey = buildRedisKey(sysTypeEnum, userEmail);
        // 读取当前错误次数（优化：先判断key是否存在，不存在则初始化，避免冗余set）
        Integer currentCount = getCurrentErrorCount(redisKey);
        // 校验错误次数阈值，超出则直接锁定
        if (currentCount > TIMES_CHECK_INPUT_PASSWORD_NUM) {
            throw new BusinessException("密码输入错误十次，已限制登录30分钟");
        }
        try {
            // 密码匹配验证
            if (StrUtil.isBlank(encodedPassword) || !passwordEncoder.matches(rawPassword, encodedPassword)) {
                // 优化：使用Redis incr原子操作，解决高并发count++非原子问题
                long newCount = RedisUtil.incr(redisKey, 1L);
                // 设置过期时间（仅在首次递增时设置，避免重复覆盖）
                if (newCount == 1) {
                    RedisUtil.expire(redisKey, CACHE_EXPIRE_SECONDS);
                }
                throw new BusinessException("账号或密码不正确");
            }
            // 优化：密码验证成功，清空错误次数缓存
            RedisUtil.del(redisKey);
        } catch (BusinessException e) {
            // 保证异常场景下缓存过期时间有效（兜底）
            if (RedisUtil.hasKey(redisKey)) {
                RedisUtil.expire(redisKey, CACHE_EXPIRE_SECONDS);
            }
            throw e;
        }
    }

    /**
     * 构建Redis缓存键
     * @param sysTypeEnum 系统类型
     * @param userEmail 用户邮箱
     * @return 完整Redis键
     */
    private String buildRedisKey(SysTypeEnum sysTypeEnum, String userEmail) {
        return sysTypeEnum.getValue()
                + CHECK_VALID_CODE_NUM_PREFIX
                + IpHelperUtil.getIpAddr()
                + "_" + userEmail;
    }

    /**
     * 获取当前错误次数（优化缓存初始化逻辑）
     * @param redisKey Redis键
     * @return 错误次数
     */
    private Integer getCurrentErrorCount(String redisKey) {
        if (RedisUtil.hasKey(redisKey)) {
            // 存在则直接获取
            return RedisUtil.get(redisKey);
        } else {
            // 不存在则初始化（仅初始化一次，避免冗余set）
            RedisUtil.set(redisKey, 0, CACHE_EXPIRE_SECONDS);
            return 0;
        }
    }
}