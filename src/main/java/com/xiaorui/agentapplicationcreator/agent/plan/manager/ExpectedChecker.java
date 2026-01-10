package com.xiaorui.agentapplicationcreator.agent.plan.manager;

import com.xiaorui.agentapplicationcreator.agent.plan.entity.ExpectedCondition;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @description: 期望条件检查器
 * @author: xiaorui
 * @date: 2026-01-05 21:11
 **/
@Component
public class ExpectedChecker {

    public static void check(Path path, ExpectedCondition expected) {

        switch (expected.getType()) {

            case CONTENT_EQUALS -> {
                if (!Files.exists(path)) {
                    throw new IllegalStateException("文件不存在，无法校验内容");
                }
                try {
                    String actual = Files.readString(path);
                    if (!actual.equals(expected.getValue())) {
                        throw new IllegalStateException(
                                "文件内容不匹配，期望：[" +
                                        expected.getValue() + "]，实际：[" + actual + "]"
                        );
                    }
                } catch (IOException e) {
                    throw new RuntimeException("读取文件失败", e);
                }
            }

            case FILE_EXISTS -> {
                if (!Files.exists(path)) {
                    throw new IllegalStateException("期望文件存在，但实际不存在");
                }
            }

            case FILE_NOT_EXISTS -> {
                if (Files.exists(path)) {
                    throw new IllegalStateException("期望文件不存在，但实际已存在");
                }
            }

            default -> throw new IllegalArgumentException("未知 expected 类型");
        }
    }
}

