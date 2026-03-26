package com.xiaorui.agentapplicationcreator.service;

import com.mybatisflex.core.service.IService;
import com.xiaorui.agentapplicationcreator.model.entity.UserThreadBind;

import java.util.List;

/**
 * 用户-智能体会话绑定表 服务层。
 *
 * @author xiaorui
 */
public interface UserThreadBindService extends IService<UserThreadBind> {

    /**
     * 绑定用户与 threadId
     *
     * @param userId 用户id
     * @param threadId 线程id
     * @param agentName 智能体名称
     */
    void bindThread(String userId, String threadId, String agentName);

    /**
     * 确保 thread 归属当前用户。
     * 未绑定则创建绑定，已绑定当前用户则直接放行，已绑定其他用户则拒绝。
     *
     * @param userId 用户id
     * @param threadId 线程id
     * @param agentName 智能体名称
     */
    void ensureThreadOwnership(String userId, String threadId, String agentName);

    /**
     * 校验 thread 是否属于 user
     *
     * @param userId 用户id
     * @param threadId 线程id
     * @return true 是，false 否
     */
    boolean validateThreadOwner(String userId, String threadId);

    /**
     * 获取用户的所有 thread
     *
     * @param userId 用户id
     * @return thread 列表
     */
    List<String> listUserThreads(String userId);

}
