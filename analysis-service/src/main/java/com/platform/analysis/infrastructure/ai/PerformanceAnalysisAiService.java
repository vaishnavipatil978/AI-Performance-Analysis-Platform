package com.platform.analysis.infrastructure.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.analysis.domain.model.AnalysisFinding;
import com.platform.analysis.domain.model.FindingSeverity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PerformanceAnalysisAiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String modelName;

    public PerformanceAnalysisAiService(
            @Qualifier("openRouterRestClient") RestClient restClient,
            ObjectMapper objectMapper,
            @Value("${spring.ai.openai.chat.options.model:anthropic/claude-3.5-sonnet}") String modelName) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.modelName = modelName;
    }

    public List<AnalysisFinding> analyseReport(String filePath, String tool) {
        String reportContent = readFileContent(filePath);
        String prompt = buildPrompt(reportContent, tool);

        Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.3,
                "max_tokens", 4096
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            return parseFindings(content);
        } catch (Exception e) {
            log.error("AI API call failed for filePath={}: {}", filePath, e.getMessage());
            return Collections.emptyList();
        }
    }

    public String getModelName() {
        return modelName;
    }

    private String readFileContent(String filePath) {
        try {
            String content = Files.readString(Paths.get(filePath));
            return content.length() > 15000 ? content.substring(0, 15000) + "\n...[truncated]" : content;
        } catch (Exception e) {
            log.warn("Could not read file at path: {}", filePath, e);
            return "[File content unavailable: " + e.getMessage() + "]";
        }
    }

    private String buildPrompt(String reportContent, String tool) {
        return """
                You are an expert performance engineer analysing a %s performance test report.

                Analyse the following report data and identify performance bottlenecks, issues, and optimisation opportunities.

                Report Content:
                %s

                Provide your analysis as a JSON array of findings. Each finding must have these exact fields:
                - severity: one of CRITICAL, HIGH, MEDIUM, LOW, INFO
                - category: e.g. "Response Time", "Error Rate", "Throughput", "Concurrency", "Resource Utilisation"
                - title: short descriptive title (max 100 chars)
                - description: detailed explanation of the issue
                - recommendation: specific actionable recommendation to fix or improve
                - affectedEndpoint: the specific endpoint/transaction affected (null if global)
                - metricValue: the observed metric value (null if not applicable)
                - thresholdValue: the recommended threshold (null if not applicable)

                Respond with ONLY the JSON array, no additional text or markdown.
                Example format: [{"severity":"HIGH","category":"Response Time","title":"...","description":"...","recommendation":"...","affectedEndpoint":"/api/users","metricValue":"2500ms","thresholdValue":"500ms"}]
                """.formatted(tool, reportContent);
    }

    @SuppressWarnings("unchecked")
    private List<AnalysisFinding> parseFindings(String aiResponse) {
        try {
            String cleaned = aiResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```json\\n?", "").replaceAll("```\\n?", "").trim();
            }
            List<Map<String, Object>> rawFindings = objectMapper.readValue(cleaned, new TypeReference<>() {});
            return rawFindings.stream().map(raw -> AnalysisFinding.builder()
                    .severity(parseSeverity((String) raw.get("severity")))
                    .category((String) raw.get("category"))
                    .title((String) raw.get("title"))
                    .description((String) raw.get("description"))
                    .recommendation((String) raw.get("recommendation"))
                    .affectedEndpoint((String) raw.get("affectedEndpoint"))
                    .metricValue((String) raw.get("metricValue"))
                    .thresholdValue((String) raw.get("thresholdValue"))
                    .build()).toList();
        } catch (Exception e) {
            log.error("Failed to parse AI findings JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private FindingSeverity parseSeverity(String severity) {
        try {
            return FindingSeverity.valueOf(severity.toUpperCase());
        } catch (Exception e) {
            return FindingSeverity.INFO;
        }
    }
}
