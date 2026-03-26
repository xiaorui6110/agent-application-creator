# Agent 上下文记忆策略

本文档对应路线图 `P2-2 完善上下文记忆策略`。

## 1. 当前结论

当前系统存在三类“记忆”或“历史”概念，但职责必须分开：

- `RedisSaver`
  - 运行态主记忆
  - 由 Spring AI Alibaba Agent Framework 按 `threadId` 持久化
  - 用于多轮对话时恢复 Agent 上下文
- `xr_chat_history`（MySQL）
  - 业务历史主存
  - 用于页面回显、审计追溯、失败补偿、后续上下文构建
- `UserThreadBind`
  - 线程归属关系
  - 用于控制 `threadId` 只能被所属用户继续使用

## 2. 不再作为主链路记忆的内容

历史上的自定义 Redis key：

- `xiaorui_user_memory:*`
- `xiaorui_ai_memory:*`

这套方案当前没有稳定消费方，也与 `RedisSaver` 的职责重复，因此不再视为主链路记忆来源。

兼容接口 `loadChatHistoryToRedis()` 仍保留，但仅作为旧调用过渡，不再向自定义 Redis key 写入聊天记录。

## 3. 责任边界

### 3.1 RedisSaver

- 负责 Agent 运行时多轮上下文
- 以 `threadId` 为核心键
- 不承担业务历史查询接口职责

### 3.2 MySQL ChatHistory

- 负责业务可见聊天历史
- 用户消息和 AI 消息都必须持久化
- 允许按 `appId` 查询最近历史
- 是补偿、重放、追溯的可靠来源

### 3.3 UserThreadBind

- 负责 `threadId -> userId` 归属绑定
- 新 thread 首次使用时创建绑定
- 已绑定当前用户时允许继续使用
- 已绑定其他用户时必须拒绝

## 4. 本次代码收口

- 新增 `ensureThreadOwnership()`，统一“绑定或校验 thread 归属”逻辑
- `AgentAppCreator` 与 `DefaultAgentOrchestrator` 不再各自重复 `bind + validate`
- `ChatHistoryService` 新增 `listRecentChatHistory()`，明确 MySQL 是业务历史主存
- `loadChatHistoryToRedis()` 降级为兼容接口，不再写自定义 Redis 聊天 key

## 5. 后续建议

- 如果后续要做“失败重试带上下文重放”，应优先基于 MySQL 最近历史构造补偿输入，而不是恢复旧的手工 Redis 聊天 key
- 如果要做跨会话总结记忆，应新增独立的 summary / profile 存储，而不是继续混用 chat history 与运行态 checkpoint
