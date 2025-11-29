package com.xiaorui.agentapplicationcreator.common;

import lombok.Data;

/**
 * @description: 通用分页请求类
 * @author: xiaorui
 * @date: 2025-11-29 11:41
 **/
@Data
public class PageRequest {
    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "descend";

}
