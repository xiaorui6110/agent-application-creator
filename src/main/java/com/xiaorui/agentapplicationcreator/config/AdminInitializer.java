package com.xiaorui.agentapplicationcreator.config;

import com.mybatisflex.core.query.QueryWrapper;
import com.xiaorui.agentapplicationcreator.constant.UserConstant;
import com.xiaorui.agentapplicationcreator.enums.UserStatusEnum;
import com.xiaorui.agentapplicationcreator.mapper.UserMapper;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static com.xiaorui.agentapplicationcreator.constant.LogicDeletedConstant.LOGIC_DELETED_NO;

/**
 * 项目启动时初始化管理员账户
 *
 * @author xiaorui
 */
@Slf4j
@Configuration
public class AdminInitializer implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@system.com";

    private static final String ADMIN_PASSWORD = "12345678";

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 检查管理员账户是否已存在
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_email", ADMIN_EMAIL)
                    .eq("is_deleted", LOGIC_DELETED_NO);
            User existingAdmin = userMapper.selectOneByQuery(queryWrapper);

            if (existingAdmin != null) {
                log.info("管理员账户已存在，无需创建: {}", ADMIN_EMAIL);
                return;
            }

            // 创建管理员账户
            User admin = new User();
            admin.setNickName("admin");
            admin.setUserEmail(ADMIN_EMAIL);
            admin.setLoginPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setUserRole(UserConstant.ADMIN_ROLE);
            admin.setUserStatus(UserStatusEnum.NORMAL.getValue());
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            admin.setIsDeleted(LOGIC_DELETED_NO);

            int result = userMapper.insert(admin);
            if (result > 0) {
                log.info("管理员账户创建成功: {}, 密码: {}", ADMIN_EMAIL, ADMIN_PASSWORD);
            } else {
                log.error("管理员账户创建失败");
            }
        } catch (Exception e) {
            log.error("初始化管理员账户失败", e);
        }
    }
}
