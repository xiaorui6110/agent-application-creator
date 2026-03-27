# 模型 Token 获取与可观测性实现说明

> 本文档基于当前项目真实实现整理，说明平台如何获取模型调用的 `model_name`、`prompt_tokens`、`completion_tokens`、`total_tokens`、耗时以及上下文信息，并将其落库后在后台可观测性面板展示。

---

## 1. 目标

当前平台需要解决两个问题：

1. 如何从模型调用过程中拿到消耗信息
2. 如何把这些信息和平台业务上下文关联起来，供管理员查看

这里的“消耗信息”主要包括：

- 模型名称 `model_name`
- 输入 Token 数 `prompt_tokens`
- 输出 Token 数 `completion_tokens`
- 总 Token 数 `total_tokens`
- 调用耗时 `latency_ms`
- 调用状态 `SUCCESS / FAILED`

这里的“业务上下文”主要包括：

- 用户 ID `user_id`
- 应用 ID `app_id`
- 对话线程 ID `thread_id`
- Agent 名称 `agent_name`

---

## 2. 现状判断

项目当前实际上有两条模型调用链：

### 2.1 Spring AI Alibaba 调用链

主 Agent 和代码优化 Agent 主要走这条链：

- `DashScopeChatModel`
- `ReactAgent`
- `appCreatorAgent.call(...)`
- `codeOptimizationAgent.call(...)`

这条链的特点是：

- 对业务开发友好
- 可以接入 Graph / Agent / Tool / Memory
- 模型返回结果最终会被包装成 `ChatResponse`

关键点在于：

`spring-ai-alibaba-dashscope` 并不是完全拿不到 token 信息。  
在当前依赖版本里，`ChatResponseMetadata.getUsage()` 是可用的，底层会映射成 `DashScopeAiUsage`。

也就是说，真正的问题不是“框架不支持”，而是：

**业务层原本没有在合适的位置把 usage 拿出来并持久化。**

### 2.2 DashScope SDK 流式调用链

项目里还有一条直接调用 DashScope SDK 的流式链：

- `AgentAppCreator.streamChat(...)`
- `Generation`
- `GenerationParam`
- `GenerationResult`

这条链更直接，`GenerationResult.getUsage()` 本身就能拿到：

- `inputTokens`
- `outputTokens`
- `totalTokens`

所以这条链不是“拿不到”，而是要手动把 usage 记到业务日志表里。

---

## 3. 整体设计思路

为了把“模型消耗”做成后台面板，而不是只停留在日志里，当前实现采用了两层思路：

### 3.1 模型调用时采集

在模型调用真正发生的地方，把 usage 和上下文拿出来。

### 3.2 业务表落库

把采集结果保存到 `xr_model_call_log`，供后台分页查询和统计聚合。

这样做的原因是：

- 比纯日志更适合后台运营查看
- 比只接 Micrometer / Prometheus 更适合当前项目的管理后台场景
- 方便按用户、应用、线程、Agent、模型筛选

---

## 4. 核心实现结构

## 4.1 数据表

新增表：

- `xr_model_call_log`

建表脚本：

- `db/20260327_add_model_call_log.sql`

核心字段：

- `user_id`
- `app_id`
- `thread_id`
- `agent_name`
- `provider`
- `model_name`
- `call_type`
- `call_status`
- `prompt_tokens`
- `completion_tokens`
- `total_tokens`
- `latency_ms`
- `error_message`
- `create_time`

---

## 4.2 后端实体与服务

相关类：

- `model/entity/ModelCallLog.java`
- `model/dto/modelcall/ModelCallLogQueryRequest.java`
- `model/vo/ModelCallStatsVO.java`
- `mapper/ModelCallLogMapper.java`
- `service/ModelCallLogService.java`
- `service/impl/ModelCallLogServiceImpl.java`
- `controller/ModelCallLogController.java`

作用分工：

- `ModelCallLog`：模型调用记录实体
- `ModelCallLogService`：落库与统计查询
- `ModelCallLogController`：后台面板接口

当前后台接口：

- `GET /modelCallLog/admin/stats`
- `POST /modelCallLog/admin/list/page`

---

## 4.3 Spring AI 调用链采集点

关键类：

- `manager/monitor/ObservableChatModel.java`

设计方式：

把原始 `DashScopeChatModel` 包装成一个“可观测 ChatModel”。

### 原理

每次业务代码调用：

- `chatModel.call(prompt)`
- `chatModel.stream(prompt)`

实际上先经过 `ObservableChatModel`，再委托给真正的 `DashScopeChatModel`。

这样就可以在统一入口做：

1. 记录开始时间
2. 调用真实模型
3. 从 `ChatResponseMetadata.getUsage()` 读取 token 信息
4. 组装 `ModelCallLog`
5. 异步保存到数据库

### 关键好处

- 不需要侵入每个 Agent 调用点单独写统计逻辑
- 主 Agent、子 Agent 只要走这套 `ChatModel`，都会被统一采集

---

## 4.4 DashScope SDK 流式调用采集点

关键位置：

- `agent/creator/AgentAppCreator.java`
- `streamChat(...)`

### 原理

在流式回调中：

- `onNext` 记录最后一个 `GenerationResult`
- `onComplete` 时读取最终 `usage`
- `onError` 时记录失败日志

这样可以拿到：

- `inputTokens`
- `outputTokens`
- `totalTokens`
- 调用耗时

并写入 `xr_model_call_log`。

---

## 5. 模型名称获取流程

模型名当前不是只取一个地方，而是做了回退链。

实现位置：

- `ObservableChatModel.buildBaseLog(...)`

当前顺序：

1. `response.metadata.model`
2. `prompt.options.model`
3. `delegate.getDefaultOptions().getModel()`
4. 配置兜底值 `fallbackModelName`

这样做的原因是：

- 某些场景下 `metadata.model` 可能为空
- 某些场景下 `prompt.options.model` 没有显式带上
- 必须保证 `model_name` 字段尽量稳定不为空

---

## 6. 业务上下文获取流程

仅拿到 token 信息还不够，后台还需要知道“这次调用属于谁、属于哪个应用、属于哪个 Agent”。

当前采用：

- `MonitorContext`
- `MonitorContextHolder`

其中：

- `MonitorContext` 保存 `userId / appId / threadId / agentName`
- `MonitorContextHolder` 负责当前线程上下文持有

### 为什么之前会出现全空

最开始使用的是普通 `ThreadLocal`。  
而 `spring-ai/graph` 这条链在执行时可能发生线程切换，导致模型调用发生时拿不到之前放进去的上下文。

### 当前修复

已改为：

- `TransmittableThreadLocal`

文件：

- `manager/monitor/MonitorContextHolder.java`

这样在异步线程切换时，上下文能继续透传。

### 当前上下文写入点

主 Agent：

- `AgentAppCreator.chat(...)`
- `AgentAppCreator.chatWithUserId(...)`

代码优化 Agent：

- `CodeOptimization.codeOptimizeAsync(...)`

流式 SDK：

- `AgentAppCreator.streamChat(...)`

---

## 7. 模型配置为什么会影响 `model_name`

项目里曾出现过：

- 数据库里记录为 `qwen-plus`
- 但配置文件里写的是 `qwen3-coder-plus`

根因不是“展示错误”，而是：

自定义 `DashScopeConfig` 最开始只注入了 `apiKey`，没有把：

- `spring.ai.dashscope.chat.options.model`
- `stream`
- `incremental-output`
- `temperature`

这些配置真正传给 `DashScopeChatModel`。

所以主链实际会回落到框架默认模型。

### 当前修复

文件：

- `agent/config/DashScopeProperties.java`
- `agent/config/DashScopeConfig.java`

现在会显式构造：

- `DashScopeChatOptions`

并设置：

- `model`
- `stream`
- `incrementalOutput`
- `temperature`

所以：

- 实际调用模型
- 可观测性日志里的 `model_name`

现在会保持一致。

---

## 8. 后台面板展示流程

前端页面：

- `app-creator-fronted/src/pages/admin/AdminObservabilityPage.vue`

前端接口：

- `app-creator-fronted/src/api/modelCallLogController.ts`

后台路由：

- `/admin/observability`

管理员顶部导航入口：

- `GlobalHeader.vue`

### 当前可展示内容

- 模型调用总数
- 今日调用数
- 成功率
- 失败率
- 输入 Token 总量
- 输出 Token 总量
- 总 Token 数
- 平均耗时
- 分页明细列表

### 当前支持的筛选

- 用户 ID
- 应用 ID
- 线程 ID
- Agent 名称
- 模型名称
- 调用状态

---

## 9. 一次完整调用的链路

下面以主 Agent 为例：

1. 用户在前端发起一次应用生成/修改请求
2. 后端进入 `AgentAppCreator.chat(...)`
3. 写入 `MonitorContext(userId, appId, threadId, agentName)`
4. 调用 `appCreatorAgent.call(...)`
5. 底层进入 `ObservableChatModel.call(...)`
6. `ObservableChatModel` 记录开始时间
7. 真正调用 `DashScopeChatModel`
8. 模型返回 `ChatResponse`
9. 从 `ChatResponseMetadata.getUsage()` 读取 token 信息
10. 从上下文读取 `userId/appId/threadId/agentName`
11. 组装 `ModelCallLog`
12. 异步落库到 `xr_model_call_log`
13. 后台 `/admin/observability` 查询该表并展示

---

## 10. 为什么不直接只用 Prometheus

项目当前配置里已经暴露了：

- `management.endpoints.web.exposure.include=health,info,prometheus`

这说明平台本身有接 Micrometer / Prometheus 的方向。

但是当前“后台可观测性面板”的需求更偏业务管理：

- 管理员想看某个用户调用了什么模型
- 想看某个应用线程的消耗
- 想看某个 Agent 的失败记录

这类需求直接查业务表更合适。

所以当前方案是：

- 业务侧先落 `xr_model_call_log`
- 后续如果需要长期时序监控，再补充 Micrometer 指标输出

这两者并不冲突。

---

## 11. 当前限制

目前这套实现已经可用，但仍有边界：

### 11.1 旧数据不会自动修复

如果历史记录是在修复前写入的，可能存在：

- `model_name` 不正确
- `user_id/app_id/thread_id/agent_name` 为空

这类旧数据需要单独回填 SQL。

### 11.2 依赖正确的上下文写入

只要某条新调用链绕过了：

- `ObservableChatModel`
- 或没有写入 `MonitorContext`

就仍可能出现业务上下文字段为空。

### 11.3 当前统计粒度仍以“单次调用日志”聚合为主

目前面板更偏运营概览，还没有做：

- 按天趋势图
- 按模型分组排行
- 按 Agent 分组排行
- 单用户累计消耗榜单

这些属于下一阶段可继续扩展的内容。

---

## 12. 总结

当前项目获取模型 token 信息的核心原理是：

1. Spring AI 链路从 `ChatResponseMetadata.getUsage()` 取 token
2. DashScope SDK 流式链路从 `GenerationResult.getUsage()` 取 token
3. 通过 `MonitorContext` 把业务上下文和模型调用关联起来
4. 统一落库到 `xr_model_call_log`
5. 后台管理页面直接查库做统计和展示

因此，当前平台已经不是“只能知道模型返回了什么”，而是可以进一步知道：

- 这次调用是谁发起的
- 属于哪个应用和线程
- 走的是哪个 Agent
- 实际调用了哪个模型
- 消耗了多少 Token
- 是否失败
- 平均耗时如何

这就是当前“模型调用消耗可观测性”的完整闭环。
