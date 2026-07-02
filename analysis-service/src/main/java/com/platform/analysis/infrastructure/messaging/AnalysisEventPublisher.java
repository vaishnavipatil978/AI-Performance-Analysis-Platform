package com.platform.analysis.infrastructure.messaging;

import com.platform.analysis.domain.event.AnalysisCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AnalysisEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String analysisCompletedTopic;

    public AnalysisEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topic.analysis-completed:analysis-completed}") String analysisCompletedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.analysisCompletedTopic = analysisCompletedTopic;
    }

    public void publishAnalysisCompleted(AnalysisCompletedEvent event) {
        kafkaTemplate.send(analysisCompletedTopic, event.reportId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish AnalysisCompletedEvent for analysisId={}", event.analysisId(), ex);
                    } else {
                        log.info("Published AnalysisCompletedEvent for analysisId={}", event.analysisId());
                    }
                });
    }
}
