package com.xiaorui.agentapplicationcreator.agent.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: xiaorui
 * @date: 2026-01-05 16:45
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResult {

    /** 是否验证通过 */
    private boolean verified;

    /** 期望值（可选） */
    private String expected;

    /** 实际值（可选） */
    private String actual;

    /** 错误信息（如读取失败） */
    private String error;
}
