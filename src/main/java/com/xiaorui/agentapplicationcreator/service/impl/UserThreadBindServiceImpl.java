package com.xiaorui.agentapplicationcreator.service.impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.mapper.UserThreadBindMapper;
import com.xiaorui.agentapplicationcreator.model.entity.UserThreadBind;
import com.xiaorui.agentapplicationcreator.service.UserThreadBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.xiaorui.agentapplicationcreator.constants.LogicDeletedConstant.LOGIC_DELETED_NO;
import static com.xiaorui.agentapplicationcreator.constants.UserConstant.BIND_STATUS_BOUND;

/**
 * 用户-智能体会话绑定表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class UserThreadBindServiceImpl extends ServiceImpl<UserThreadBindMapper, UserThreadBind>  implements UserThreadBindService{

    /**
     * 绑定用户与 threadId
     *
     * @param userId 用户id
     * @param threadId 线程id
     * @param agentName 智能体名称
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindThread(String userId, String threadId, String agentName) {
        try {
            // 先校验 threadId 是否已被绑定
            long count =  QueryChain.of(UserThreadBind.class)
                    .eq(UserThreadBind::getThreadId, threadId)
                    .eq(UserThreadBind::getBindStatus, BIND_STATUS_BOUND)
                    .eq(UserThreadBind::getIsDeleted, LOGIC_DELETED_NO)
                    .count();
            if (count > 0) {
                // 已绑定，直接返回（幂等）
                return;
            }
            UserThreadBind userThreadBind = new UserThreadBind();
            userThreadBind.setUserId(userId);
            userThreadBind.setThreadId(threadId);
            userThreadBind.setAgentName(agentName);
            userThreadBind.setBindStatus(BIND_STATUS_BOUND);
            userThreadBind.setIsDeleted(LOGIC_DELETED_NO);
            save(userThreadBind);
        } catch (Exception e) {
            log.error("userBindThread error: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "绑定用户与 threadId 失败");
        }
    }

    /**
     * 校验 thread 是否属于 user
     *
     * @param userId 用户id
     * @param threadId 线程id
     * @return true 是，false 否
     */
    @Override
    public boolean validateThreadOwner(String userId, String threadId) {
        return QueryChain.of(UserThreadBind.class)
                .eq(UserThreadBind::getUserId, userId)
                .eq(UserThreadBind::getThreadId, threadId)
                .eq(UserThreadBind::getBindStatus, BIND_STATUS_BOUND)
                .eq(UserThreadBind::getIsDeleted, LOGIC_DELETED_NO)
                .exists();
    }

    /**
     * 获取用户的所有 thread
     *
     * @param userId 用户id
     * @return thread 列表
     */
    @Override
    public List<String> listUserThreads(String userId) {
        return QueryChain.of(UserThreadBind.class)
                .eq(UserThreadBind::getUserId, userId)
                .eq(UserThreadBind::getBindStatus, BIND_STATUS_BOUND)
                .eq(UserThreadBind::getIsDeleted, LOGIC_DELETED_NO)
                .list()
                .stream()
                .map(UserThreadBind::getThreadId)
                .toList();
    }

}
