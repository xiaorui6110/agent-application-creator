package com.xiaorui.agentapplicationcreator.agentexample;


import cn.hutool.core.lang.UUID;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description: agent 调用测试，参考最新官方文档 <a href="https://java2ai.com/docs/frameworks/agent-framework/tutorials/agents">...</a>
 * @author: xiaorui
 * @date: 2025-12-10 14:14
 **/
@SpringBootTest
public class MiniAppCreatorTest {

    @Resource
    private ReactAgent appCreatorAgent;

    @Test
    public void testAppCreatorAgent() throws GraphRunnerException {

        // 生成一个随机的 threadId
        String threadId = UUID.randomUUID().toString();

        // threadId 是给定对话的唯一标识符，使用 threadId 维护对话上下文
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).addMetadata("user_id", "1").build();

        // 调用 Agent（第一次调用）
        AssistantMessage response = appCreatorAgent.call("帮我做一个简易的在线图书管理系统，先给出初步的设计方案吧，尽量简单些。", runnableConfig);

        // 打印结果（方便调试）
        System.out.println("Agent Response: " + response.getText());

        // 注意我们可以使用相同的 threadId 继续对话（第二次调用）
        response = appCreatorAgent.call("谢谢你呀^^!", runnableConfig);
        System.out.println(response.getText());

    }

/*

### 第一次调用

Agent Response: 好的，已为你解析需求。我们即将构建一个**简易但完整、可运行的在线图书管理系统**，适合学习、演示或小型团队使用。

---

## 📚 项目名称：**BookNest Lite**
> 一句启动，三端就绪 —— 轻如羽毛，强如钢骨。

---

### ✅ 设计目标

- **极简可用**：只保留核心功能（增删改查）
- **前后端分离**：前端 Vue3 + 后端 Spring Boot
- **快速部署**：支持本地一键运行，未来可容器化
- **教学友好**：结构清晰，注释充分，适合初学者理解全栈流程

---

## 🧱 系统架构概览

```
+------------------+       +------------------+
|   用户浏览器      | <---> |     Vue3 前端     |
| (http://localhost:3000) | (Vite dev server) |
+------------------+       +------------------+
                                    ↓ (HTTP API)
                            +------------------+
                            |  Spring Boot API   |
                            | (http://localhost:8080) |
                            +------------------+
                                    ↓ (JDBC)
                           +--------------------+
                           |    MySQL 数据库     |
                           |    (book_nest_db)   |
                           +--------------------+
```

> 可选集成 Redis 缓存（后续扩展用）

---

## 📦 功能模块设计（MVP 版本）

| 模块 | 功能 | 说明 |
|------|------|------|
| 图书管理 | 查看列表、新增、编辑、删除 | 核心 CRUD |
| 搜索功能 | 按书名/作者模糊搜索 | 前端输入 → 后端 LIKE 查询 |
| 数据持久化 | 使用 MySQL 存储图书信息 | 单表设计 |

---

## 🗃️ 数据库设计（`book` 表）

```sql
CREATE DATABASE IF NOT EXISTS book_nest_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE book_nest_db;

CREATE TABLE `book` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `title` VARCHAR(255) NOT NULL COMMENT '书名',
  `author` VARCHAR(100) NOT NULL COMMENT '作者',
  `isbn` VARCHAR(20) UNIQUE COMMENT 'ISBN编号',
  `publish_year` YEAR COMMENT '出版年份',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 🔌 API 接口设计（RESTful 风格）

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/books` | 获取所有图书（支持 keyword 搜索） |
| POST | `/api/books` | 新增一本图书 |
| PUT | `/api/books/{id}` | 更新图书信息 |
| DELETE | `/api/books/{id}` | 删除图书 |
| GET | `/api/books/{id}` | 获取单本书详情（预留） |

---

## 💻 前端页面结构（Vue3 + Element Plus）

- `App.vue` - 主布局
- `views/BookList.vue` - 图书列表页（含搜索框 + 表格 + 操作按钮）
- `components/AddBookDialog.vue` - 添加/编辑弹窗
- `api/book.js` - 封装 axios 请求
- `store/index.js` - 使用 Pinia 进行状态管理（轻量级）

> UI 框架：[Element Plus](https://element-plus.org/) —— 快速出效果，不折腾样式

---

## ⚙️ 技术栈总结

| 层级 | 技术选型 |
|------|----------|
| 前端 | Vue3 + Vite + Pinia + Element Plus + Axios |
| 后端 | Spring Boot 3.x + Spring Web + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 构建工具 | npm / Maven |
| 开发环境 | JDK 17+, Node.js 16+, MySQL 8+ |

---

## 📁 初步目录结构预览

```bash
book-nest/
│
├── backend/                    # Spring Boot 项目
│   ├── src/
│   │   ├── main/java/com/example/booknest/
│   │   │   ├── BookNestApplication.java
│   │   │   ├── controller/BookController.java
│   │   │   ├── service/BookService.java
│   │   │   ├── mapper/BookMapper.java
│   │   │   └── model/Book.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── mapper/BookMapper.xml
│   └── pom.xml
│
├── frontend/                   # Vue3 项目
│   ├── src/
│   │   ├── views/BookList.vue
│   │   ├── components/AddBookDialog.vue
│   │   ├── api/book.js
│   │   ├── store/modules/bookStore.js
│   │   └── App.vue
│   ├── index.html
│   └── vite.config.js
│
├── docs/
│   └── DESIGN.md               # 设计文档
│
├── docker-compose.yml          # 可选：一键启动 MySQL
├── README.md
└── .env                        # 环境变量配置
```

---

## ▶️ 下一步建议

你现在有两个选择：

1. **我继续帮你生成完整的可运行代码**（前端 + 后端 + 数据库脚本），你可以直接下载运行。
2. 或者你想先了解某个部分的实现细节？比如：
   - 如何在 Vue 中调用 API？
   - 如何用 MyBatis-Plus 实现分页？
   - 怎么防止重复提交？

告诉我你的偏好 👇
是要「一键生成完整项目」，还是「分步讲解某一部分」？

> 我随时准备把想法变成现实。✨


### 第二次调用


不客气呀～✨
你的每个想法都值得被温柔以待，而我，就在这里把它们变成可运行的现实 🚀

有想法随时丢过来——无论是“搞个后台管理系统”还是“做个会聊天的小程序”，我都能给你一套完整的方案，还能陪你一步步理解它、优化它、驾驭它。

一起 coding 吧，让灵感落地生花 💡

Process finished with exit code 0

 */
}
