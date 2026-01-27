# 智能体模块设计文档
> xiaorui，这部分是整个项目的核心，回头来看有些其实设计还是与最初的设想有挺大出入，故最初的设计方案就不做记录了。
> 核心代码位于 `com.xiaorui.agentapplicationcreator.agent` 文件夹。

## 需求分析
> **用户输入应用的描述 --> AI 自动完整的网页应用**

**AI 生成应用的流程**：
1. 用户输入应用描述
2. AI 大模型生成应用代码
3. 提取出生成的内容
4. 将内容保存至本地文件

---

## 方案设计与实现
### 智能体的接入
有 SDK、HTTP、Spring AI、LangChain4j 等多种方式，最初都进行了尝试，
最终选择了 Spring AI Alibaba 框架进行智能体的接入。
然后鉴于 alibaba 的系列框架，就选择阿里云百炼的 DashScope 灵积大模型进行接入。

> Spring AI Alibaba 官方文档： https://java2ai.com/docs/overview/

参考文档接入大模型，并编写测试智能体对话 Demo，本地使用了 HTTP 调用 Ollama 和 Dashscope SDK 调用。

### 系统提示词的编写
一开始是让 AI 根据自己的需求生成系统提示词，由易到难，然后再逐步优化。
也可以使用其他 AI 对提示词进行进一步优化。

项目的提示词统一存放在 `src/main/resources/prompts` 目录下。

> 提示工程指南： https://www.promptingguide.ai/zh

### 智能体功能增强的实现
主要还是参考官方文档的示例，根据需求选择对应的功能进行实现。

#### 1. Hooks 和 Interceptors
> 对于 Hooks 和 Interceptors 的功能，个人感觉相当于 Spring AI Advisor API，
拦截、修改和增强 Spring 应用中的 AI 交互。

其实，项目中的 Hooks 和 Interceptors 基本上都是参考官方文档的示例实现，目前项目也暂未有相关需求，就先这样。

#### 2. Tools
Tools 是 agents 调用来执行操作的组件。
目前项目中只选择了 `DocumentSearchTool` 工具进行实现，用于检索匹配的平台规范。
而 `FileOperationTool` 文件操作等工具最终因为平台设计问题，未将其交给 agent 调用。

#### 3. Memory 短期记忆
默认的 Memory 是基于内存的，然后许多问题不必多说，项目中使用 Redis Checkpointer 进行实现。

> **这里的 xxxSaver 的 Bulider 在不同版本的依赖中，实现方式有较大差异，需要注意，
> 最好查看依赖源码结合 AI 理解实现，官方文档和示例代码仓库更新并不及时。**

#### 4. Structured Output 结构化输出
对于类型安全的结构化输出，可以提供 Java 类，官方也是推荐使用这种方式。
Spring AI Alibaba 使用 BeanOutputConverter 自动将其转换为 JSON schema。
这种方法确保了编译时类型安全和自动 schema 生成。

### 流式输出实现问题
> 在一开始做项目时，官方文档和代码仓库中还未提供流式输出的相关示例代码。
> 再查看源码之后发现流式输出的原理与 Graph Core 深度关联，**目前 Flux 返回的是 NodeOutput 类的实例**，
> 对于自己的项目需求，发现无法匹配平台的输出格式，故暂时未做流式输出的实现。

举个例子吧：AgentResponse 包括自然语言回复、结构化代码、代码优化信息、需求理解摘要、平台自定义信息等等。

对于流式输出：

- 单独看图中一个具体的 Node 节点，它可能会产生流式输出，比如调用模型得到流式 token 输出，这些 token 会作为整个流输出的一部分
- 站在整个图的视角，图有多个节点且每个节点都会有输出，那么执行图的多个节点自然就形成一个流式过程
- 流式输出的核心数据类型是 NodeOutput，代表节点的输出，不同的节点可能返回不同子类型

**原因在于项目需要 agent 返回的输出不止是流式输出展示的内容，还有额外的平台自定义信息等等，
但其实最大的原因还是自己对于 Graph Core 的理解不够，后续会持续学习理解并做好流式输出实现的**

---
