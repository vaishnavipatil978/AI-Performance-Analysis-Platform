package com.platform.notification.infrastructure.messaging;

import com.platform.common.util.CorrelationIdUtil;
import com.platform.notification.application.service.NotificationService;
import com.platform.notification.domain.event.AnalysisCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisCompletedConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${app.kafka.topic.analysis-completed:analysis-completed}",
            groupId = "${app.kafka.consumer.group-id:notification-service-group}"
    )
    public void consume(AnalysisCompletedEvent event) {
        CorrelationIdUtil.set(event.correlationId());
        try {
            log.info("Received AnalysisCompletedEvent for analysisId={}", event.analysisId());
            notificationService.handleAnalysisCompleted(event);
        } finally {
            CorrelationIdUtil.clear();
        }
    }
}
