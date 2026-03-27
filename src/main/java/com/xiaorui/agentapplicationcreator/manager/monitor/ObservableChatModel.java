package com.xiaorui.agentapplicationcreator.manager.monitor;

import com.xiaorui.agentapplicationcreator.model.entity.ModelCallLog;
import com.xiaorui.agentapplicationcreator.service.ModelCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class ObservableChatModel implements ChatModel {

    private static final String PROVIDER = "dashscope";

    private final ChatModel delegate;
    private final ModelCallLogService modelCallLogService;
    private final String fallbackModelName;

    @Override
    public ChatResponse call(Prompt prompt) {
        MonitorContext context = MonitorContextHolder.getContext();
        long start = System.currentTimeMillis();
        try {
            ChatResponse response = delegate.call(prompt);
            modelCallLogService.record(buildSuccessLog(context, prompt, response, "SYNC",
                    System.currentTimeMillis() - start));
            return response;
        } catch (Exception e) {
            modelCallLogService.record(buildFailedLog(context, prompt, "SYNC",
                    System.currentTimeMillis() - start, e));
            throw e;
        }
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return delegate.getDefaultOptions();
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        MonitorContext context = MonitorContextHolder.getContext();
        long start = System.currentTimeMillis();
        AtomicReference<ChatResponse> lastResponseRef = new AtomicReference<>();
        AtomicBoolean recorded = new AtomicBoolean(false);
        return delegate.stream(prompt)
                .doOnNext(lastResponseRef::set)
                .doOnError(error -> {
                    if (recorded.compareAndSet(false, true)) {
                        modelCallLogService.record(buildFailedLog(context, prompt, "STREAM",
                                System.currentTimeMillis() - start, error));
                    }
                })
                .doOnComplete(() -> {
                    if (recorded.compareAndSet(false, true)) {
                        modelCallLogService.record(buildSuccessLog(context, prompt, lastResponseRef.get(),
                                "STREAM", System.currentTimeMillis() - start));
                    }
                });
    }

    private ModelCallLog buildSuccessLog(MonitorContext context, Prompt prompt, ChatResponse response,
                                         String callType, long latencyMs) {
        ChatResponseMetadata metadata = response == null ? null : response.getMetadata();
        Usage usage = metadata == null ? null : metadata.getUsage();
        return buildBaseLog(context, prompt, metadata, callType, latencyMs)
                .callStatus("SUCCESS")
                .promptTokens(usage == null || usage.getPromptTokens() == null ? 0 : usage.getPromptTokens())
                .completionTokens(usage == null || usage.getCompletionTokens() == null ? 0 : usage.getCompletionTokens())
                .totalTokens(usage == null || usage.getTotalTokens() == null ? 0 : usage.getTotalTokens())
                .build();
    }

    private ModelCallLog buildFailedLog(MonitorContext context, Prompt prompt, String callType,
                                        long latencyMs, Throwable error) {
        return buildBaseLog(context, prompt, null, callType, latencyMs)
                .callStatus("FAILED")
                .promptTokens(0)
                .completionTokens(0)
                .totalTokens(0)
                .errorMessage(truncate(error == null ? null : error.getMessage()))
                .build();
    }

    private ModelCallLog.ModelCallLogBuilder buildBaseLog(MonitorContext context, Prompt prompt,
                                                          ChatResponseMetadata metadata, String callType,
                                                          long latencyMs) {
        String modelName = metadata == null ? null : metadata.getModel();
        if ((modelName == null || modelName.isBlank()) && prompt != null && prompt.getOptions() != null) {
            modelName = prompt.getOptions().getModel();
        }
        if ((modelName == null || modelName.isBlank()) && delegate.getDefaultOptions() != null) {
            modelName = delegate.getDefaultOptions().getModel();
        }
        if (modelName == null || modelName.isBlank()) {
            modelName = fallbackModelName;
        }
        return ModelCallLog.builder()
                .userId(context == null ? null : context.getUserId())
                .appId(context == null ? null : context.getAppId())
                .threadId(context == null ? null : context.getThreadId())
                .agentName(context == null ? null : context.getAgentName())
                .provider(PROVIDER)
                .modelName(modelName)
                .callType(callType)
                .latencyMs(latencyMs);
    }

    private String truncate(String message) {
        if (message == null || message.length() <= 1000) {
            return message;
        }
        return message.substring(0, 1000);
    }
}
