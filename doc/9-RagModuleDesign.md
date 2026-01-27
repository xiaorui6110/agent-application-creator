# 检索增强生成（RAG）模块设计文档
> xiaorui，本文档有一些自己与 GPT 讨论的关于 RAG 设计的内容，很容易看出来的。

## 设计原因
由于之前对于不同的应用代码生成方式（单文件、前端三件套、Vue 工程），都编写了对应的详细 prompt。
其中包含了更加详细的应用代码生成规范等等，想将其复用在项目中，但是系统的 prompt 又不宜过长。
最终就考虑将其作为 RAG 信息检索模块，当作应用生成的规范，让 agent 搜索对应规范生成代码。

> Retrieval Augmented Generation (RAG) 是一种有用的技术，
> 用于克服大型语言模型的局限性，
> 这些模型在处理长文本内容、事实准确性和上下文感知方面存在困难。

## Agentic RAG

Agentic 检索增强生成（RAG）将检索增强生成的优势与基于 Agent 的推理相结合。
Agent（由 LLM 驱动）不是在回答之前检索文档，
而是逐步推理并决定在交互过程中何时以及如何检索信息。

| 特征                 | 是否适合 RAG |
|--------------------|----------|
| 稳定、不频繁变化           | ✅        |
| 属于“规范 /经验 /最佳实践”   | ✅        |
| 不需要每次都用            | ✅        |
| 和生成模式强相关           | ✅        |
| 希望 agent“参考，而不是照抄” | ✅        |

> 官方文档： https://java2ai.com/docs/frameworks/agent-framework/advanced/rag#agentic-rag

---

逻辑非常干净：

1. 用户确认生成方式 = `generationMode`
2. 主 Agent：

    * 用 `generationMode` 作为 query
    * 检索对应 spec
3. 将命中的 spec：

    * 注入到 **阶段 3 的上下文**
    * 而不是 system prompt

### 实现流程

对于标准的 RAG 开发流程，步骤如下：
1. 文档收集和切割 
2. 向量转换和存储 
3. 切片过滤和检索 
4. 查询增强和关联

> 以上标准的流程基本上是基于 Python 的，然后自己对应基于自己的项目就简化了一下实现。

**简化后的 RAG 开发流程**：
1. 文档准备 
2. 文档读取 
3. 向量转换和存储
4. 查询增强

> 对于向量库等细节的设计信息，可以查看对应代码的注释，此处不做过细的说明。

## 统一 metadata schema

统一 metadata schema 的本质是：

> 不给 RAG 喂“散文 prompt”，而是喂“有标签的工程知识块”。

现在有三类 spec：
- SINGLE_FILE 
- MULTI_FILE 
- VUE_PROJECT

如果只是把它们原文丢进向量库：
- 检索只能靠“语义相似度” 
- Agent 问的是：「我要生成 Vue 项目」 
- 向量库回的是：「这里有一段 HTML 的最佳实践」

——这就是 RAG 用着用着变蠢的原因。

**metadata schema = 给知识加“工程坐标系”。**
```
{
  "specId": "FRONTEND_VUE_PROJECT_V1",
  "category": "FRONTEND_APP_GENERATION",
  "generationMode": "VUE_PROJECT",
  "appType": "ENGINEERED_FRONTEND",
  "techStack": ["Vue3", "Vite"],
  "stage": ["SOLUTION_DESIGN", "CODE_GENERATION"],
  "priority": 10,
  "version": "1.0.0",
  "status": "ACTIVE"
}
```


