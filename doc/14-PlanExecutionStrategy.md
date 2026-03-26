# Plan 执行能力策略

本文档对应路线图 `P2-3 完善计划执行能力`。

## 1. 当前目标

计划执行链的核心目标不是“让 Agent 能改更多文件”，而是：

- 修改计划可校验
- 执行过程可失败定位
- 执行失败可回滚
- 主链路只允许进入安全、可解释的文件操作

## 2. 当前主链路允许的操作

当前 `DefaultPlanValidator` 仅允许以下操作进入主链路：

- `CREATE_FILE`
- `OVERWRITE_FILE`
- `APPEND_FILE`
- `DELETE_FILE`
- `MOVE_FILE`
- `RENAME_FILE`
- `CREATE_DIRECTORY`
- `DELETE_EMPTY_DIRECTORY`

以下类型不再允许直接进入主链路执行：

- `READ_FILE`
- `EXISTS`
- `SEARCH_BY_NAME`
- `SEARCH_BY_CONTENT`
- `DELETE_DIRECTORY_RECURSIVE`
- 其他未明确支持的操作

原因：

- 查询类操作不应混入“修改计划执行器”
- 高危目录删除不适合作为主链路自动执行能力
- 主链路应优先保持最小可控操作面

## 3. 执行前校验

每个操作在执行前必须满足：

- 路径已被限制在 `code_output` 根目录内
- `expected` 条件明确
- 必填字段完整
- 操作类型属于主链路允许子集

此外，执行前会再次检查 `expected`，避免在文件状态变化后继续盲写。

## 4. 回滚策略

当前执行器在每个操作执行前都会记录快照：

- 原路径是否存在
- 原路径原始内容
- 目标路径是否存在
- 目标路径原始内容

一旦出现以下任一情况：

- 某一步操作执行失败
- 最终 `verification` 校验失败

执行器会按逆序回滚已执行操作。

## 5. 执行结果增强

当前 `ExecutionResult` 增加了以下可观测字段：

- `failedOperationIndex`
- `rolledBack`

`OperationResult` 增加：

- `rolledBack`

这样可以明确知道：

- 失败发生在哪一步
- 是否已经执行回滚
- 单步操作是否已被回撤

## 6. 后续建议

- 如果后续要支持“页面元素级修改”，应优先增强计划生成质量与 `expected` 精度，而不是继续扩充危险操作类型
- 如果要支持批量多文件修改，建议补充事务日志或临时工作区机制，而不是直接把更多写操作塞进当前执行器
