package com.xiaorui.agentapplicationcreator.ollamaexample;

import cn.hutool.json.JSONObject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @description: HTTP 调用 ollama api 测试 <a href="https://docs.ollama.com/api/introduction">...</a>
 * @author: xiaorui
 * @date: 2025-12-09 13:04
 **/
@SpringBootTest
public class OllamaApiTest {

    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";

    private CloseableHttpClient httpClient;

    /**
     * 每个测试方法执行前初始化 HTTP 客户端
     */
    @BeforeEach
    void setUp() {
        // 创建 HTTP 客户端实例
        httpClient = HttpClients.createDefault();
    }

    /**
     * 每个测试方法执行后关闭 HTTP 客户端
     */
    @AfterEach
    void tearDown() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    /**
     * 核心测试方法：本地 HTTP 调用 Ollama /api/generate 接口（无需 API KEY）
     */
    @Test
    void testCallOllamaApi() throws IOException, ParseException {
        // 1. 构建请求参数
        JSONObject requestParams = new JSONObject();
        // 模型名称
        requestParams.set("model", "gemma3:4b");
        // 提问内容
        requestParams.set("prompt", "你好呀，我是 xiaorui ^^");
        // 可选：关闭流式返回（默认流式，如需同步返回完整结果可添加）
        // requestParams.put("stream", false);

        // 2. 创建 HTTP POST 请求
        HttpPost httpPost = new HttpPost(OLLAMA_API_URL);
        // 设置请求体（JSON 格式）
        StringEntity requestEntity = new StringEntity(
                requestParams.toString(),
                ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)
        );
        httpPost.setEntity(requestEntity);

        // 3. 执行请求并获取响应
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // 4. 解析响应结果（Ollama 流式响应是多行 JSON，逐行读取）
            String responseContent = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            System.out.println("=== Ollama API 响应结果 ===");

            // 分割流式响应（兼容 Windows/Linux 换行符）
            String[] responseLines = responseContent.split("\\r?\\n");
            for (String line : responseLines) {
                if (line.isBlank()) {
                    continue;
                }

                // 安全解析 JSON（防止单行非 JSON 格式）
                JSONObject responseJson;
                try {
                    responseJson = new JSONObject(line);
                } catch (Exception e) {
                    System.out.println("跳过非 JSON 响应行：" + line);
                    continue;
                }

                // 1. 获取回答内容（空值安全处理）
                String answer = responseJson.getStr("response", "");
                if (!answer.isBlank()) {
                    System.out.print(answer);
                }

                // 2. 判断是否为最后一行（done=true）
                boolean isDone = responseJson.getBool("done", false);
                if (isDone) {
                    System.out.println("\n=== 响应结束 ===");

                    // 核心修复：metadata 非空判断
                    JSONObject metadata = responseJson.getJSONObject("metadata");
                    if (metadata != null) {
                        // 安全获取数值（无值时返回 0）
                        Long totalDuration = metadata.getLong("total_duration", 0L);
                        Long promptEvalCount = metadata.getLong("prompt_eval_count", 0L);
                        Long evalCount = metadata.getLong("eval_count", 0L);

                        System.out.println("总耗时（纳秒）：" + totalDuration);
                        System.out.println("提示词令牌数：" + promptEvalCount);
                        System.out.println("回答令牌数：" + evalCount);
                    } else {
                        System.out.println("响应无 metadata 元数据");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("API 调用异常：" + e.getMessage());
            // 断言失败，抛出异常
            org.junit.jupiter.api.Assertions.fail("调用 Ollama API 失败：" + e.getMessage());
        }
    }

    @Test
    void testCallOllamaApiWithException() {
        try {
            // 故意使用不存在的模型
            JSONObject requestParams = new JSONObject();
            requestParams.set("model", "non-existent-model");
            requestParams.set("prompt", "Why is the sky blue?");

            HttpPost httpPost = new HttpPost(OLLAMA_API_URL);
            httpPost.setEntity(new StringEntity(requestParams.toString(), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                org.junit.jupiter.api.Assertions.assertNotEquals(200, response.getCode(), "预期调用失败");
            }
        } catch (Exception e) {
            System.out.println("调用失败，异常信息：" + e.getMessage());
            org.junit.jupiter.api.Assertions.assertTrue(e instanceof IOException, "预期抛出 IO 异常");
        }
    }
}
