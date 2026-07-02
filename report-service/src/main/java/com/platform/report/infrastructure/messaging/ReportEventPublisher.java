package com.platform.report.infrastructure.messaging;

import com.platform.report.domain.event.ReportUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String reportUploadedTopic;

    public ReportEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topic.report-uploaded:report-uploaded}") String reportUploadedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.reportUploadedTopic = reportUploadedTopic;
    }

    public void publishReportUploaded(ReportUploadedEvent event) {
        kafkaTemplate.send(reportUploadedTopic, event.reportId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish ReportUploadedEvent for reportId={}", event.reportId(), ex);
                    } else {
                        log.info("Published ReportUploadedEvent for reportId={}", event.reportId());
                    }
                });
    }
}
