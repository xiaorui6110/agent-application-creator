# RAG 接口留白与边界策略

本文档对应路线图 `P2-5 RAG 接入前先做接口留白收口`。

## 1. 当前目标

当前阶段不追求把 RAG 做重，而是先把调用边界固定下来，避免后续替换向量库、元数据存储或检索策略时改动主链路。

## 2. 稳定边界

当前主链路只依赖一层稳定接口：

- `SpecSearchService`

输入：

- `SpecSearchRequest`
  - `query`
  - `generationMode`
  - `stage`

输出：

- `SpecSearchResult`
  - `content`
  - `matchedSpecIds`

这意味着：

- Agent 工具层不直接依赖 `VectorStore`
- Agent 工具层不直接依赖 `MetadataManager`
- 后续切换为 Redis / DB / 外部向量库时，主链路只需要保留该接口契约

## 3. 当前实现分层

### 3.1 DocumentSearchTool

- 只负责把 Agent 工具请求适配到 `SpecSearchService`
- 不再持有底层检索实现细节

### 3.2 SpecSearchService

- 负责组织一次完整规范检索
- 负责初始化、候选过滤、向量召回、内容拼接

### 3.3 MetadataManager

- 负责 spec 初始化
- 负责 metadata 过滤
- 负责按 specId 回表加载原文

## 4. 当前修复

本次还顺带修了两个实现问题：

- `AppCreatorAgentConfig` 原先手动 `new DocumentSearchTool()`，会导致 Spring 注入失效
- `DocumentLoader` 原先没有真正构建 `SpecMetadata` 所需字段，导致后续 metadata 过滤不稳定

现在：

- `DocumentSearchTool` 通过 Spring Bean 注入
- spec 文件会补出：
  - `specId`
  - `generationMode`
  - `stage`
  - `techStack`
  - `status`

## 5. 后续建议

- 如果下一阶段接入外部向量库，只替换 `SpecSearchService` 内部实现
- 如果后续要引入多类知识源，应新增新的检索服务接口，而不是继续把所有知识都塞进 `DocumentSearchTool`
