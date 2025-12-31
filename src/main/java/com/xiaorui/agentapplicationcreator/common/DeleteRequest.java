package com.xiaorui.agentapplicationcreator.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 删除请求包装类
 * @author: xiaorui
 * @date: 2025-12-31 14:08
 **/
@Data
public class DeleteRequest implements Serializable {

    /**
     * id（String 类型的id，通常是主键id）
     */
    private String id;

    @Serial
    private static final long serialVersionUID = 1L;

}