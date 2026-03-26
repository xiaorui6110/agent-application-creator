# Sub-Agent 与代码优化能力策略

本文档对应路线图 `P2-4 整理 sub-agent 和 code optimization 能力`。

## 1. 当前定位

`CodeOptimization` 不再视为在线主流程必经能力，而是：

- 主任务完成后的异步可选增强
- 用于沉淀代码优化建议与平台经验
- 失败时只影响优化结果，不影响用户本次生成主链路

## 2. 主链路与副链路边界

### 2.1 主链路

主链路只负责：

- 接收用户请求
- 调用主 Agent
- 保存生成代码
- 保存主任务结果

### 2.2 副链路

副链路只负责：

- 基于主 Agent 输出的 `codeOptimizationInput`
- 异步调用 `codeOptimizationAgent`
- 持久化优化结果到 `xr_code_optimize_result`

## 3. 触发时机

当前触发条件：

- 主任务已经成功拿到 `SystemOutput`
- 主 Agent 返回了 `codeOptimizationInput`

然后由 `AgentTaskExecutor.submitOptimizationTask()` 异步触发，不阻塞主任务返回。

## 4. 稳定性原则

- 副 agent 失败不回滚主任务
- 副 agent 超时不影响用户本次应用生成
- 副 agent 的结果只作为后续会话的附加上下文，不作为本次主链路成功条件

## 5. 本次代码收口

- `DefaultAgentOrchestrator` 在主任务成功后异步提交代码优化任务
- `CodeOptimization` 去掉重复的 `@Async` 边界，统一由 `AgentTaskExecutor` 承担异步调度
- `CodeOptimization` 改为通过 `CodeOptimizeResultService` 持久化，而不是直接依赖 Mapper
- 增加输入校验，避免把不完整的优化输入送入副 agent

## 6. 后续建议

- 如果后续发现优化结果质量不稳，应继续保持“异步增强”定位，而不是拉回主链路
- 如果要让主 Agent 消费优化结果，建议只消费摘要和平台经验，不要直接把大段建议全文拼进每次 prompt
