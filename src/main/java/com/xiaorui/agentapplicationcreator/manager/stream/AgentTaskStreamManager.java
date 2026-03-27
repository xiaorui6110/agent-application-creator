package com.xiaorui.agentapplicationcreator.manager.stream;

import com.xiaorui.agentapplicationcreator.agent.model.dto.AgentStreamEvent;
import com.xiaorui.agentapplicationcreator.agent.model.schema.SystemOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class AgentTaskStreamManager {

    private static final Long SSE_TIMEOUT_MS = 10 * 60 * 1000L;

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String taskId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitterMap.computeIfAbsent(taskId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(taskId, emitter));
        emitter.onTimeout(() -> removeEmitter(taskId, emitter));
        emitter.onError(ex -> removeEmitter(taskId, emitter));
        return emitter;
    }

    public void publishConnected(String taskId, SystemOutput snapshot) {
        publish(taskId, buildEvent("connected", snapshot, snapshot != null ? snapshot.getMessage() : "stream connected", false));
    }

    public void publishStatus(String taskId, SystemOutput snapshot) {
        publish(taskId, buildEvent("status", snapshot, snapshot != null ? snapshot.getMessage() : null, false));
    }

    public void publishProgress(String taskId, String threadId, String appId, String taskStatus, String message, String agentName) {
        AgentStreamEvent event = AgentStreamEvent.builder()
                .event("progress")
                .taskId(taskId)
                .threadId(threadId)
                .appId(appId)
                .taskStatus(taskStatus)
                .message(message)
                .agentName(agentName)
                .done(false)
                .timestamp(System.currentTimeMillis())
                .build();
        publish(taskId, event);
    }

    public void publishDone(String taskId, SystemOutput output) {
        publish(taskId, buildEvent("done", output, output != null ? output.getMessage() : "task succeeded", true));
        complete(taskId);
    }

    public void publishFailed(String taskId, SystemOutput snapshot) {
        publish(taskId, buildEvent("failed", snapshot, snapshot != null ? snapshot.getTaskError() : "task failed", true));
        complete(taskId);
    }

    @Scheduled(fixedRate = 15000)
    public void heartbeat() {
        emitterMap.forEach((taskId, emitters) -> {
            if (emitters == null || emitters.isEmpty()) {
                emitterMap.remove(taskId);
                return;
            }
            AgentStreamEvent heartbeat = AgentStreamEvent.builder()
                    .event("heartbeat")
                    .taskId(taskId)
                    .message("keep-alive")
                    .done(false)
                    .timestamp(System.currentTimeMillis())
                    .build();
            publish(taskId, heartbeat);
        });
    }

    private AgentStreamEvent buildEvent(String eventName, SystemOutput snapshot, String message, boolean done) {
        return AgentStreamEvent.builder()
                .event(eventName)
                .taskId(snapshot != null ? snapshot.getTaskId() : null)
                .threadId(snapshot != null ? snapshot.getThreadId() : null)
                .appId(snapshot != null ? snapshot.getAppId() : null)
                .taskStatus(snapshot != null ? snapshot.getTaskStatus() : null)
                .message(message)
                .agentName(snapshot != null ? snapshot.getAgentName() : null)
                .done(done)
                .timestamp(System.currentTimeMillis())
                .result(done ? snapshot : null)
                .build();
    }

    private void publish(String taskId, AgentStreamEvent event) {
        List<SseEmitter> emitters = emitterMap.get(taskId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(event.getEvent())
                        .data(event));
            } catch (IOException e) {
                log.warn("Send SSE event failed, taskId={}, event={}", taskId, event.getEvent(), e);
                emitter.completeWithError(e);
                removeEmitter(taskId, emitter);
            }
        }
    }

    private void complete(String taskId) {
        List<SseEmitter> emitters = emitterMap.remove(taskId);
        if (emitters == null) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            emitter.complete();
        }
    }

    private void removeEmitter(String taskId, SseEmitter emitter) {
        emitterMap.computeIfPresent(taskId, (key, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }
}
