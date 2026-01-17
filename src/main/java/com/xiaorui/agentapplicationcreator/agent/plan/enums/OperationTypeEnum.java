package com.xiaorui.agentapplicationcreator.agent.plan.enums;

import lombok.Getter;

/**
 * @description: 文件操作枚举（GPT）
 * @author: xiaorui
 * @date: 2026-01-05 20:30
 **/
@Getter
public enum OperationTypeEnum {

    /* ===================== 文件操作 ===================== */

    /** 创建文件（不存在时）并写入内容 */
    CREATE_FILE,

    /** 覆盖文件内容（必须存在） */
    OVERWRITE_FILE,

    /** 追加写入文件内容 */
    APPEND_FILE,

    /** 读取文件内容 */
    READ_FILE,

    /** 删除文件 */
    DELETE_FILE,

    /** 移动文件（可跨目录） */
    MOVE_FILE,

    /** 重命名文件（语义上是 move 的特化） */
    RENAME_FILE,

    /* ===================== 目录操作 ===================== */

    /** 创建目录（递归） */
    CREATE_DIRECTORY,

    /** 删除空目录 */
    DELETE_EMPTY_DIRECTORY,

    /** 递归删除目录（高危，建议 PlanValidator 严控） */
    DELETE_DIRECTORY_RECURSIVE,

    /** 列出目录结构（tree） */
    LIST_DIRECTORY_TREE,

    /* ===================== 查询 / 搜索 ===================== */

    /** 判断文件或目录是否存在 */
    EXISTS,

    /** 按文件名关键词搜索 */
    SEARCH_BY_NAME,

    /** 按内容关键词搜索（grep） */
    SEARCH_BY_CONTENT,

    /* ===================== 校验 / 验证 ===================== */

    /** 验证文件内容是否等于预期 */
    VERIFY_CONTENT_EQUALS,

    /** 验证文件是否包含某段内容 */
    VERIFY_CONTENT_CONTAINS
}
