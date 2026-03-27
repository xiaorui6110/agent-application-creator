# 开始阅读本项目文档前，请先看这份说明

> 本文档用于说明当前仓库的真实定位、主链路、已完成能力、阅读顺序和后续关注点。  
> 如果你只打算先快速建立全局认知，这一篇应该最先看。

---

## 1. 项目定位

本项目是一个基于 `Spring Boot 3 + Spring AI Alibaba + Vue 3` 构建的：

**面向网页 / 小应用生成场景的 Agent 应用生成平台**

它的重点不是做一个完全通用的 Agent 基础框架，而是围绕“用户创建应用并持续迭代”的完整业务闭环来设计系统。

当前已经落地的核心目标是：

1. 用户输入自然语言需求创建应用
2. Agent 根据需求生成或修改代码
3. 前端展示对话结果与应用预览
4. 用户继续多轮对话迭代应用
5. 用户下载代码或一键部署
6. 平台提供模板、社区、后台运营与可观测能力

---

## 2. 当前项目已经具备的能力

### 2.1 用户侧

- 用户注册、登录、重置密码
- 首页自然语言创建应用
- 应用对话生成与继续修改
- 应用预览、下载、部署
- 应用版本快照与恢复
- 模板广场、模板创建、基于模板创建应用
- 应用评论、点赞、分享
- 社区通知与个人设置

### 2.2 平台侧

- 用户管理
- 应用管理
- 对话管理
- 任务监控与失败任务重试
- 运营概览面板
- 模型调用可观测性面板

### 2.3 Agent 相关

- 主 Agent 应用生成
- 结构化输出协议解析与校验
- 代码修改计划校验与执行
- 副 Agent 代码优化
- 线程绑定与运行态上下文记忆
- 文档 / 规范类 RAG 支撑
- 异步任务执行与状态轮询

---

## 3. 当前主链路

这是当前项目最重要的一条链路，也是后续所有优化默认优先服务的对象：

1. 用户在首页输入需求并创建应用
2. 系统创建应用记录并进入应用对话页
3. 用户发送消息给 Agent
4. 后端创建异步任务并执行业务编排
5. Agent 返回结构化结果，系统将代码落盘
6. 前端轮询任务状态并刷新预览
7. 用户继续修改、下载或部署

如果后续你要继续优化项目，默认应该优先保证这条链路稳定、可追踪、可恢复。

---

## 4. 当前架构认知

### 4.1 前端

前端位于：

- `app-creator-fronted/app-creator-fronted`

主要页面包括：

- `HomePage.vue`：首页、模板广场、精选应用、我的作品
- `AppChatPage.vue`：应用对话、生成、预览、部署、下载
- `AppEditPage.vue`：应用编辑与版本相关能力
- `UserCommunityPage.vue`：社区中心
- 后台管理页：运营概览、用户管理、应用管理、对话管理、任务监控、可观测性面板

### 4.2 后端

后端位于：

- `src/main/java/com/xiaorui/agentapplicationcreator`

主要模块包括：

- `controller`：用户、应用、任务、社区、模型调用日志等接口
- `service`：核心业务实现
- `agent`：Agent 编排、协议、计划执行、RAG、sub-agent
- `manager`：任务、监控、鉴权、部署等管理能力
- `mapper`：MyBatis-Flex 数据访问
- `model`：DTO / Entity / VO
- `config` / `util`：系统配置与工具类

### 4.3 平台扩展能力

当前项目已经不只是 CRUD，而是已经具备以下平台能力：

- 异步任务机制
- 线程归属绑定
- 模板沉淀与复用
- 评论 / 点赞 / 分享互动
- 任务监控
- 运营概览
- 模型 Token 可观测性

---

## 5. 当前文档体系怎么读

如果你希望快速理解项目，建议按下面顺序阅读：

### 第一阶段：先建立整体认知

1. `0-MustFirstRead.md`
2. `11-ProjectImprovementRoadmap.md`
3. `README.md`

### 第二阶段：理解主链路与 Agent 设计

1. `3-AgentModuleDesign.md`
2. `4-AppModuleDesign.md`
3. `12-AgentInputOutputProtocol.md`
4. `13-AgentMemoryStrategy.md`
5. `14-PlanExecutionStrategy.md`
6. `15-SubAgentStrategy.md`
7. `16-RagBoundaryStrategy.md`

### 第三阶段：理解平台扩展能力

1. `18-P3VersionManagement.md`
2. `19-P3AppTemplate.md`
3. `20-P3CommunityCapability.md`
4. `21-P3RecommendAndRanking.md`
5. `22-ModelTokenObservability.md`

### 第四阶段：理解迭代收口情况

1. `17-P2ClosureReport.md`
2. `11-ProjectImprovementRoadmap.md`
3. `CurrentOptimizationPoints.md`

---

## 6. 当前阶段的真实判断

这个项目现在已经不是“只有概念”的阶段，而是已经具备较完整的演示和答辩能力：

- 用户主链路基本完整
- 平台扩展能力已经落地一部分
- 后台管理与运营能力已经成型
- 可观测性能力已经能落到页面展示

但也要明确几点：

- 项目仍然是“聚焦生成场景的 Agent 平台”，不是通用智能体平台
- 某些能力已经设计得很前，但实现成熟度并不完全一致
- 流式输出、匿名访问体验、长期记忆等方向仍有继续优化空间

所以后续演进建议仍然遵循这个原则：

1. 先稳主链路
2. 再补平台能力
3. 最后再做更复杂的 Agent 扩展

---

## 7. 当前值得关注的文档

如果你只想看当前最有价值、最贴近现状的几篇，优先看这几份：

- `11-ProjectImprovementRoadmap.md`
- `12-AgentInputOutputProtocol.md`
- `13-AgentMemoryStrategy.md`
- `14-PlanExecutionStrategy.md`
- `17-P2ClosureReport.md`
- `18-P3VersionManagement.md`
- `19-P3AppTemplate.md`
- `20-P3CommunityCapability.md`
- `22-ModelTokenObservability.md`

---

## 8. 一句话结论

当前仓库已经可以被看作一个：

**围绕“创建应用 -> 对话生成 -> 预览 -> 修改 -> 下载 / 部署”主链路构建，并逐步补齐模板、社区、运营、可观测能力的 Agent 应用生成平台。**

阅读后续文档时，请始终带着这个定位去理解各个模块，而不要把它误解成一个完全通用的 Agent 基础框架。
