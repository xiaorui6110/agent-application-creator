# single_file_spec.md

（Single HTML Application Generation Spec）

---

## Spec ID

`FRONTEND_SINGLE_FILE_V1`

---

## 适用范围（Scope）

本规范适用于 **SINGLE_FILE 模式** 的前端应用生成：

* 单个 `.html` 文件
* 所有功能纯前端
* 无构建工具、无工程化依赖
* 适合 Demo、工具页、小型交互应用

---

## 技术栈约束（Tech Stack Constraints）

* **HTML5**
* **CSS3**
* **原生 JavaScript（Vanilla JS）**

### 禁止项（Hard Prohibitions）

* 任何外部 CSS 框架（如 Bootstrap、Tailwind）
* 任何外部 JS 库（如 jQuery、React、Vue）
* 外部字体库、图标库
* 任何服务端或后端逻辑

---

## 文件结构规范（File Structure）

* **只能输出一个 `.html` 文件**
* 所有 CSS：

  * 必须内联在 `<head>` 中的 `<style>` 标签内
* 所有 JavaScript：

  * 必须放在 `</body>` 之前的 `<script>` 标签内
* 不允许引用任何外部文件

---

## 布局与响应式规范（Layout & Responsiveness）

* 页面必须是响应式的
* 优先使用：

  * Flexbox
  * CSS Grid
* 不允许通过 JS 进行布局计算

---

## 内容填充规范（Content Placeholder Policy）

当用户未提供具体内容时：

* 文本：

  * 使用语义合理的占位文本（如 Lorem Ipsum）
* 图片：

  * 使用 `https://picsum.photos`
  * 必须包含 `alt` 属性

---

## 交互实现规范（Interaction Rules）

* 所有交互逻辑必须使用原生 JavaScript
* 常见交互示例：

  * Tab 切换
  * 表单提交提示
  * 图片轮播
* 不允许引入 polyfill 或第三方工具

---

## 代码质量规范（Code Quality）

* HTML 结构语义化、层级清晰
* CSS 有基本注释，避免魔法值
* JS：

  * 函数职责单一
  * 避免全局变量污染
* 允许适量注释，但不冗余

---

## 安全与边界（Security Boundary）

* 所有逻辑必须是纯客户端行为
* 不涉及任何鉴权、数据持久化或网络请求假设

---

## 平台偏好（Platform Preferences）

* 优先选择**简单直观的实现**
* 避免过度抽象
* 保证生成代码可被普通前端开发者快速理解

---

## 反模式（Anti-Patterns）

* 将大量 JS 写成匿名代码块
* 在 HTML 中混入复杂业务逻辑
* 为简单需求设计“类框架结构”

---
