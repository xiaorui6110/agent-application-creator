# P3-1 应用版本管理

这一阶段的目标不是直接做复杂的版本协作平台，而是先把应用代码版本的最小闭环补齐，便于后续继续扩展 diff、compare、template 等能力。

## 1. 能力目标

- 在 Agent 主生成成功后自动创建 `GENERATED` 版本快照
- 在应用部署成功后自动创建 `DEPLOYED` 版本快照
- 支持查看应用历史版本列表
- 支持将指定版本恢复回当前 `code_output/{appId}` 目录
- 为后续补充 `RESTORED`、版本对比和模板沉淀预留基础能力

## 2. 存储设计

- 数据表使用 `xr_app_version`
- 快照目录使用 `app.storage.code-version-dir`
- 默认路径为 `./tmp/code_version`

目录结构约定如下：

- `{appId}/v{versionNumber}`

## 3. 对外接口

- `GET /app/version/list/{appId}`
- `POST /app/version/restore`

## 4. 当前约束

- 当前版本管理先聚焦“快照保存与恢复”，还不包含 diff 能力
- 恢复操作会直接覆盖 `code_output/{appId}` 下的当前代码目录
- 暂未提供版本删除、版本备注编辑和版本对比页面

## 5. 后续扩展

- 可以继续向以下方向扩展
- 增加版本 diff
- 增加版本比对结果展示
- 增加恢复后的再次快照记录
- 与模板化、社区共享能力联动
