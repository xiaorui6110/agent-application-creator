# Agent 输入输出协议

本文档对应路线图 `P2-1 Agent 协议收口`，用于明确当前主 Agent 的输入输出边界。

## 1. 输入协议

当前主入口请求对象为 `CallAgentRequest`，核心字段如下：

- `message`
  - 用户原始输入
  - 仅承载用户意图，不应混入系统控制字段
- `threadId`
  - 可选
  - 用于延续同一轮多轮对话上下文
- `appId`
  - 应用级上下文标识
  - 用于将对话、生成结果、代码优化结果绑定到同一应用

输入侧约束：

- `message` 不能为空
- `message` 长度上限为 2000
- 敏感词命中时直接拒绝
- `threadId` 必须与当前用户绑定关系一致，否则拒绝访问

## 2. 输出协议

主 Agent 最终输出必须是 `AgentResponse` JSON。

### 2.1 顶层字段

- `responseType`
  - 表示当前输出所属阶段
  - 允许值：
    - `CLARIFICATION`
    - `MODE_SELECTION`
    - `SOLUTION_DESIGN`
    - `CODE_GENERATION`
    - `CODE_MODIFICATION`
- `reply`
  - 面向用户展示的自然语言回复
  - 禁止放代码、禁止放 JSON 片段
- `structuredReply`
  - 面向系统消费的代码生成结果
- `codeModificationPlan`
  - 面向系统消费的代码修改计划
- `codeGenType`
  - 当前代码生成模式
  - 仅允许：
    - `single_file`
    - `multi_file`
    - `vue_project`

### 2.2 互斥规则

- `structuredReply` 与 `codeModificationPlan` 不能同时出现
- `CLARIFICATION` / `MODE_SELECTION` / `SOLUTION_DESIGN`
  - 不允许返回 `structuredReply`
  - 不允许返回 `codeModificationPlan`
- `CODE_GENERATION`
  - 必须返回 `codeGenType`
  - 必须返回 `structuredReply`
- `CODE_MODIFICATION`
  - 必须返回 `codeModificationPlan`

### 2.3 StructuredReply 规则

`structuredReply` 当前用于承载代码生成结果，关键字段如下：

- `generationMode`
  - 与顶层 `codeGenType` 保持一致
- `entry`
  - 入口文件
- `files`
  - 文件路径到文件内容的映射
- `runnable`
  - 是否可直接运行
- `description`
  - 机器可读的简要说明

兼容策略：

- 保留旧字段 `type`
- 解析时会自动将 `type` 与 `generationMode` 对齐

## 3. 解析与校验策略

当前主链路在 `AgentAppCreator` 中不再直接把模型原始 JSON 当成可信输出使用，而是先经过：

1. `parse`
2. `normalize`
3. `validate`

具体由 `AgentOutputProtocolResolver` 负责，职责如下：

- 解析模型原始 JSON
- 推断老输出缺失的 `responseType`
- 统一 `codeGenType` / `generationMode` 大小写
- 对旧字段 `type` 做兼容映射
- 校验结构化输出和修改计划是否互斥
- 校验 `CODE_GENERATION` 与 `CODE_MODIFICATION` 的必填字段

## 4. 当前落地原则

- 用户看的内容只放 `reply`
- 系统执行的数据只放结构化字段
- 模型输出先校验，再进入主链路
- 旧输出尽量兼容，但新输出必须向统一协议收口
