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
public class ZKProofService {

    @Value("${zk.proof.service.url:http://127.0.0.1:3000}")
    private String zkServiceUrl;

    private final WebClient webClient;

    /**
     * Call Node.js ZK service to generate proof
     * The service wraps Midnight SDK's prove() function
     */
    public String generateProof(String candidateId, Integer privateScore, Integer requiredThreshold) {
        try {
            log.info("Calling ZK Proof service for candidate: {}, score: {}, threshold: {}",
                    candidateId, privateScore, requiredThreshold);

            String proofUrl = zkServiceUrl + "/prove";

            Map<String, Object> proofRequest = Map.of(
                    "candidate_id", candidateId,
                    "private_score", privateScore,
                    "required_threshold", requiredThreshold
            );

            Map<String, Object> response = webClient
                    .post()
                    .uri(proofUrl)
                    .bodyValue(proofRequest)
                    .retrieve()
                    .onStatus(
                            status -> status != HttpStatus.OK,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("ZK Proof service error: {}", body);
                                        return Mono.error(new RuntimeException("ZK Proof generation failed"));
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("proof")) {
                String proof = (String) response.get("proof");
                log.info("Successfully generated ZK proof for candidate: {}", candidateId);
                return proof;
            }

            log.warn("ZK Proof service returned empty or invalid response");
            return null;

        } catch (Exception e) {
            log.error("Error generating ZK proof", e);
            return null;
        }
    }

    /**
     * Verify that a ZK proof is valid
     */
    public boolean verifyProof(String proof, Integer privateScore, Integer requiredThreshold) {
        try {
            log.info("Verifying ZK proof");

            String verifyUrl = zkServiceUrl + "/verify";

            Map<String, Object> verifyRequest = Map.of(
                    "proof", proof,
                    "private_score", privateScore,
                    "required_threshold", requiredThreshold
            );

            Map<String, Object> response = webClient
                    .post()
                    .uri(verifyUrl)
                    .bodyValue(verifyRequest)
                    .retrieve()
                    .onStatus(
                            status -> status != HttpStatus.OK,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("ZK Proof verification failed: {}", body);
                                        return Mono.error(new RuntimeException("Verification failed"));
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("valid")) {
                boolean isValid = (Boolean) response.get("valid");
                log.info("ZK proof verification result: {}", isValid);
                return isValid;
            }

            return false;

        } catch (Exception e) {
            log.error("Error verifying ZK proof", e);
            return false;
        }
    }
}
