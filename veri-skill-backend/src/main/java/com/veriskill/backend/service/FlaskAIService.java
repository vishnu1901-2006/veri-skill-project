package com.veriskill.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlaskAIService {

    @Value("${flask.ai.url:http://127.0.0.1:5001}")
    private String flaskBaseUrl;

    private final WebClient webClient;

    /**
     * Call Flask AI service to score resume against job description
     * Returns integer score 0-100
     */
    public Integer scoreResume(String jobDescription, String resumeText) {
        try {
            log.info("Calling Flask AI service to score resume");

            String flaskUrl = flaskBaseUrl + "/score";

            Map<String, Object> response = webClient
                    .post()
                    .uri(flaskUrl)
                    .bodyValue(Map.of(
                            "job_description", jobDescription,
                            "resume_text", resumeText
                    ))
                    .retrieve()
                    .onStatus(
                            status -> status != HttpStatus.OK,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Flask AI service error: {}", body);
                                        return Mono.error(new RuntimeException("Flask AI service failed"));
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("score")) {
                Integer score = ((Number) response.get("score")).intValue();
                log.info("Received score from Flask AI: {}", score);
                return score;
            }

            log.warn("Flask AI service returned empty or invalid response");
            return 0;

        } catch (Exception e) {
            log.error("Error calling Flask AI service", e);
            return 0;
        }
    }
}
