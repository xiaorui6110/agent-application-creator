# 项目代码审查与整改对账报告（Phase 1）

## 1. 审查范围

- 后端：用户与认证、Agent 编排、应用管理、版本恢复、部署、模板、社区互动、静态资源访问、公共分页接口
- 前端：环境配置项与部署域名默认值
- 审查方式：静态代码审查 + 分模块整改 + 编译验证
- 当前验证范围：已多轮执行 `.\mvnw.cmd -q -DskipTests compile`

## 2. 当前总体结论

本轮高优先级安全与边界问题已经完成大部分整改，尤其是以下几类风险已经显著收口：

- 验证码伪造绕过
- 认证链路中的 token / session 身份混用
- 改密、封禁、角色变更后的 token 统一失效
- Agent 跨应用写入
- 版本恢复、模板建应用、部署、下载等链路中的访问控制缺失
- 社区通知未读清理与互动状态不一致
- 公开接口暴露敏感字段
- 预览静态资源未鉴权访问
- 多处分页总数与分页参数控制不一致

当前项目已经从“存在明显越权和身份边界问题”的状态，进入“核心风险基本收口，但仍需要补自动化测试和少量低优先级治理项”的状态。

## 3. 审查问题与整改状态

### 3.1 P0: 图片验证码可被前端伪造绕过

- 风险说明：
  - 原实现把验证码答案直接返回给前端，登录时仅比较两个前端参数是否相等，验证码实际可被伪造绕过。
- 整改状态：已完成
- 已落实修复：
  - 改为服务端 challenge 模式
  - 前端只拿到 challengeId，不再拿到真实验证码答案
  - 登录时改为服务端从 Redis 比对 challengeId 对应验证码
  - 校验后立即失效，避免重复使用
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/UserServiceImpl.java`

### 3.2 P0: AuthFilter 混用“当前会话登录态”和“请求 token”

- 风险说明：
  - 原链路先校验当前上下文是否登录，再读取另一份 access token 装载用户，存在身份混淆风险。
- 整改状态：已完成
- 已落实修复：
  - 认证统一收敛为“按当前请求 token 解析用户”
  - 不再先依赖上下文已有登录态再读取另一份 token
  - 用户上下文只围绕同一个 token 建立
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/security/filter/AuthFilter.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/util/SecurityUtil.java`

### 3.3 P1: token 全量失效链路不一致

- 风险说明：
  - 原有 loginId / uid / userStatus 等概念混用，改密、封禁、角色变更后的旧 token 可能无法完整失效。
- 整改状态：已完成
- 已落实修复：
  - loginId 统一为固定规则 `user:{userId}`
  - token 存储与全量失效逻辑统一收敛
  - 改密、重置密码、封禁、角色变更后走同一套旧 token 失效规则
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/manager/token/TokenStoreManager.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/UserServiceImpl.java`

### 3.4 P1: Agent 写代码前缺少 app 归属校验

- 风险说明：
  - 原链路在任务编排和文件写入前没有强制校验 `appId` 是否属于当前用户，存在跨应用写入风险。
- 整改状态：已完成
- 已落实修复：
  - 在任务初始化、聊天记录写入、文件落盘前统一校验 app 访问权限
  - 自动重试改为基于原始用户输入重新执行，不再依赖不稳定摘要字段
  - 重试成功后补齐文件写入和版本快照链路
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/agent/orchestrator/DefaultAgentOrchestrator.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/AgentTaskServiceImpl.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/util/CodeFileSaverUtil.java`

### 3.5 P1: 文件写入与版本恢复边界不足

- 风险说明：
  - 原实现中路径与数量限制不足，版本恢复失败时缺少补偿，容易留下半成品状态。
- 整改状态：已完成
- 已落实修复：
  - 文件写入增加 app 存在性校验、路径规范化、重复目标路径检查、数量与总大小限制
  - 版本恢复增加事务和失败回滚
  - 版本恢复后清空旧部署状态，避免“恢复本地代码但仍显示旧部署”
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/util/CodeFileSaverUtil.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/AppVersionServiceImpl.java`

### 3.6 P1: 模板、下载、部署链路缺少统一访问控制或失败补偿

- 风险说明：
  - 模板来源校验、下载权限、部署目录清理、模板建应用失败清理等原先不完整。
- 整改状态：已完成
- 已落实修复：
  - 模板创建改走统一 app 权限校验
  - 模板源代码目录完整性检查已补齐
  - 下载服务内部增加 app 访问校验
  - 部署前先清空 deploy 目录，避免旧产物残留
  - 模板创建、模板建应用、版本恢复加入事务和失败补偿
  - 模板列表默认不再公开 `sourceAppId` 和 `createdBy`
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/AppTemplateServiceImpl.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/ProjectDownloadServiceImpl.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/AppServiceImpl.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/AppVersionServiceImpl.java`

### 3.7 P2: “我的应用”分页先分页再过滤，结果失真

- 风险说明：
  - 原实现先分页查全量，再内存过滤当前用户，导致总数和 records 不一致。
- 整改状态：已完成
- 已落实修复：
  - 查询条件直接追加 `user_id = currentUserId`
  - 分页后直接返回对应记录，不再做内存过滤
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/controller/AppController.java`

### 3.8 P2: 配置与运维边界暴露风险

- 风险说明：
  - 原配置中存在真实部署地址、敏感配置管理方式不清晰等问题。
- 整改状态：部分完成
- 已落实修复：
  - 后端开发配置中的数据库、邮箱、对象存储、SFTP、模型 key 等敏感项已改为环境变量占位
  - 保留了用户确认过的真实部署公共地址 `http://172.19.48.249`
  - README 已回退，等待后续统一中文文档整理
- 尚未完全完成的点：
  - 仍需统一梳理一份正式的 `.example` 配置策略和部署说明
  - 仍需人工审查历史提交，确认历史 Git 记录中是否曾泄露真实密钥
- 主要涉及文件：
  - `src/main/resources/application-dev.yml`
  - `src/main/resources/application.yml`

### 3.9 P1: 社区评论 / 点赞 / 分享存在状态不一致和未读清理错误

- 风险说明：
  - 评论点赞信任客户端计数
  - 未读清理使用错误条件
  - controller 返回成功但后台异步任务可能失败
  - 评论查询匿名与 service 鉴权不一致
- 整改状态：已完成
- 已落实修复：
  - 评论点赞改为只按服务端真实记录更新，不再信任前端计数
  - 评论查询恢复为真正支持匿名访问
  - 点赞与分享改为同步事务执行
  - 未读清理统一改为按 ID 列表更新
  - 历史、未读、计数逻辑统一限定在有效点赞 / 分享状态
  - 社区通知聚合改为“只清理实际返回给前端的那批通知”
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/AppCommentServiceImpl.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/LikeRecordServiceImpl.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/ShareRecordServiceImpl.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/CommunityServiceImpl.java`
  - 对应 controller / service 接口

### 3.10 P1: 公开接口暴露敏感字段

- 风险说明：
  - 公开详情、推荐、排行等接口原先会返回 `appInitPrompt` 和 `recommendScore`。
- 整改状态：已完成
- 已落实修复：
  - AppVO 组装拆分为公开版和私有版
  - 公开接口不再返回 `appInitPrompt` 和 `recommendScore`
  - “我的应用”和管理员视图保留私有字段
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/AppService.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/AppServiceImpl.java`
  - `src/main/java/com/xiaorui/agentapplicationcreator/controller/AppController.java`

### 3.11 P1: 预览静态资源可被未授权访问

- 风险说明：
  - 原 `/static/preview/{appId}/**` 只要知道 `appId` 就可以读取预览目录中的产物。
- 整改状态：已完成
- 已落实修复：
  - 预览资源访问改为强制走 `appService.validateAppAccess(...)`
  - 只有应用所有者或管理员可以访问预览产物
  - 部署静态资源仍按 deployKey 公开访问，符合部署语义
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/controller/StaticResourceController.java`

### 3.12 P2: 截图服务缺少 URL 边界

- 风险说明：
  - 原截图服务接受任意 URL，未来若被复用，存在 SSRF 风险。
- 整改状态：已完成
- 已落实修复：
  - 截图服务只允许截取配置中的部署公开域名
  - 对 scheme / host / port 做一致性校验
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/service/impl/ScreenshotServiceImpl.java`

### 3.13 P2: 用户分页接口总数字段错误

- 风险说明：
  - 用户分页接口把 `totalPage` 当成 `totalRow` 返回，前端分页总数错误。
- 整改状态：已完成
- 已落实修复：
  - 改为返回真实 `totalRow`
  - 补齐公开用户查询和管理员用户列表的页大小上限与默认值
- 主要涉及文件：
  - `src/main/java/com/xiaorui/agentapplicationcreator/controller/UserController.java`

## 4. 当前剩余项

以下项目当前不属于“阻塞上线”的高优先级问题，但建议继续安排：

### 4.1 低优先级待处理

- 清理剩余未触碰公共 DTO / VO / enum / util 中的乱码注释，降低维护成本
- 检查是否仍有公开接口缺少统一分页上限
- 评估社区评论点赞是否需要真正的“按用户去重”设计
- 评估 `viewCount` 的增长链路是否需要防刷、去重或异步聚合
- 评估 deployKey 是否需要长度、轮换策略或唯一性巡检
- 统一 README、示例配置、部署文档的中文版本

### 4.2 建议补充的自动化测试

- 用户登录验证码 challenge 生命周期测试
- AuthFilter 基于请求 token 的身份装载测试
- 改密、封禁、改角色后的旧 token 失效测试
- Agent 跨 app 写入拒绝测试
- 文件写入路径逃逸、重复路径、超限数量、超限体积测试
- 版本恢复失败回滚测试
- 模板建应用失败清理测试
- 社区通知 `limit` 截断后仅清理返回项测试
- 评论点赞不信任客户端计数测试
- 公开 AppVO 不暴露 `appInitPrompt` / `recommendScore` 测试
- 预览静态资源未授权访问拒绝测试
- 用户分页 `totalRow` 正确性测试

## 5. 编译验证结果

本轮整改过程中已多次执行以下命令，当前可通过：

```powershell
.\mvnw.cmd -q -DskipTests compile
```

说明：

- 当前只完成了编译级验证
- 尚未完成系统级回归测试、接口自动化测试和真实部署链路验证

## 6. 建议的下一阶段工作

建议进入 Phase 2，重点从“补测试”和“收口低优先级治理项”两个方向推进：

1. 先为本轮已修复的高风险点补最小可用自动化测试
2. 再清理剩余公共层乱码与分页边界不一致问题
3. 最后统一 README、部署说明和 example 配置文档

## 7. 本次对账结论

截至当前，Phase 1 初始审查报告中的核心高优先级问题已基本完成整改，剩余事项主要集中在：

- 自动化测试补强
- 文档与配置规范化
- 少量低优先级公共治理项

当前建议不再继续扩大改动面，而是优先把已修复能力通过测试固化下来。
