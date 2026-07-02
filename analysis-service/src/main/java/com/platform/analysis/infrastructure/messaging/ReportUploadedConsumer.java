package com.platform.analysis.infrastructure.messaging;

import com.platform.analysis.application.service.AnalysisService;
import com.platform.analysis.domain.event.ReportUploadedEvent;
import com.platform.common.util.CorrelationIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportUploadedConsumer {

    private final AnalysisService analysisService;

    @KafkaListener(
            topics = "${app.kafka.topic.report-uploaded:report-uploaded}",
            groupId = "${app.kafka.consumer.group-id:analysis-service-group}"
    )
    public void consume(ReportUploadedEvent event) {
        CorrelationIdUtil.set(event.correlationId());
        try {
            log.info("Received ReportUploadedEvent for reportId={}", event.reportId());
            analysisService.processReportUploadedEvent(event);
        } finally {
            CorrelationIdUtil.clear();
        }
    }
}
