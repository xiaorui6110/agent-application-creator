package com.xiaorui.agentapplicationcreator.agent.rag.metadata.loader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: Markdown 文档加载器
 * @author: xiaorui
 * @date: 2026-01-17 15:46
 **/
@Slf4j
@Component
public class DocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 加载 classpath:prompt/specs/ 下所有 md 文件，就是 src/main/resources/prompt/specs 目录
            Resource[] resources = resourcePatternResolver.getResources("classpath:prompt/specs/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();

   /*             // 读取md文件的原始内容。resource.getFile()，在开发环境（IDE 本地运行） 是没问题的，
                // 但是如果项目打包成 jar 包部署后，resource.getFile() 会抛出异常（因为 jar 包内的资源不是磁盘文件）
                // String markdownContent = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);*/

                // 核心：按规则处理 md 内容 -> 过滤代码块/引用块 + 按 --- 分割文档（兼容本地IDE运行 + jar包部署，读取classpath下的资源不报错）
                String markdownContent;
                try (InputStream inputStream = resource.getInputStream();
                     BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    markdownContent = sb.toString();
                }
                List<String> splitContentList = processMarkdown(markdownContent);
                // 遍历分割后的内容，构建 Document 对象，附加元信息
                for (String content : splitContentList) {
                    if (content.isBlank()) {
                        continue;
                    }
                    // 附加 filename 元信息
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("filename", fileName);
                    allDocuments.add(new Document(content.trim(), metadata));
                }
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }

    /**
     * 核心处理方法：复刻原代码的所有规则，1:1对齐
     * 1. 过滤掉md中的代码块 ```xxx``` 内容
     * 2. 过滤掉md中的引用块 >xxx 内容
     * 3. 按水平线 --- 分割文档内容
     */
    private List<String> processMarkdown(String markdownContent) {
        if (markdownContent == null || markdownContent.isBlank()) {
            return Collections.emptyList();
        }
        // 规则1：过滤代码块 (```开头 到 ```结尾 全部删除)
        String noCodeBlock = markdownContent.replaceAll("(?s)```[\\s\\S]*?```", "");
        // 规则2：过滤引用块 (>开头的整行全部删除)
        String noQuoteBlock = noCodeBlock.replaceAll("^>.*(\\n|$)", "");
        // 规则3：按 水平线--- 分割成多个文档内容
        return Arrays.stream(noQuoteBlock.split("---"))
                .map(String::trim)
                .filter(content -> !content.isBlank())
                .collect(Collectors.toList());
    }

}
