# multi_file_spec.md

（Multi-File Static Frontend Generation Spec）

---

## Spec ID

`FRONTEND_MULTI_FILE_V1`

---

## 适用范围（Scope）

本规范适用于 **MULTI_FILE 模式** 的前端应用生成：

* 多文件静态网站
* 明确分离结构 / 样式 / 行为
* 不使用现代前端框架
* 适合中等复杂度静态应用

---

## 技术栈约束（Tech Stack Constraints）

* **HTML5**
* **CSS3**
* **原生 JavaScript（Vanilla JS）**

### 禁止项（Hard Prohibitions）

* 任何外部 CSS 框架
* 任何 JS 框架或库
* 构建工具（Webpack / Vite 等）
* 后端逻辑或 API 假设

---

## 标准文件结构（Required File Structure）

```
/
├── index.html
├── style.css
└── script.js
```

### 引用规范（Mandatory Linking Rules）

* `index.html`

  * `<head>` 中必须通过 `<link>` 引用 `style.css`
  * `</body>` 结束前必须通过 `<script>` 引用 `script.js`

---

## 文件职责边界（File Responsibility）

### index.html

* 只负责：

  * 页面结构
  * 语义化内容
* 不包含样式规则
* 不包含业务逻辑

---

### style.css

* 包含所有样式规则
* 使用 Flexbox 或 Grid 进行布局
* 必须支持桌面与移动端

---

### script.js

* 包含所有交互与业务逻辑
* 使用原生 JavaScript
* 不允许操作样式字符串拼接布局

---

## 内容填充规范（Content Placeholder Policy）

* 文本缺失时：

  * 使用语义合理的占位文本
* 图片缺失时：

  * 使用 `https://picsum.photos`
* 所有图片必须有 `alt`

---

## 响应式与可维护性（Responsiveness & Maintainability）

* 响应式是**强制要求**
* CSS 结构应可读、可扩展
* JS：

  * 避免写成“脚本式一坨”
  * 推荐函数分组、逻辑分区

---

## 代码质量规范（Code Quality）

* 合理注释（解释“为什么”，而不是“这是什么”）
* 命名清晰、一致
* 禁止复制粘贴式代码堆叠

---

## 平台偏好（Platform Preferences）

* 明确结构优先于技巧
* 可读性优先于炫技
* 为后续升级到工程化项目保留空间

---

## 反模式（Anti-Patterns）

* 在 HTML 中写 JS 逻辑
* 在 JS 中拼大量 HTML 字符串
* CSS 与 JS 职责混用

---
