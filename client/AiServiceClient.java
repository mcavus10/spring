package com.example.moodmovies.client;

import com.example.moodmovies.dto.AnalysisResponse;
import com.example.moodmovies.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Client for communicating with the Python AI service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${moodiemovies.ai-service.base-url}")
    private String aiServiceUrl;
    
    @Value("${moodiemovies.ai-service.api-key}")
    private String apiKey;
    
    /**
     * Requests personality analysis for a user from the AI service.
     *
     * @param userId The ID of the user to analyze
     * @return Analysis response from the AI service
     * @throws AiServiceException if there's an error communicating with the AI service
     */
    public AnalysisResponse requestPersonalityAnalysis(String userId) {
        try {
            log.info("Sending POST personality analysis request to AI service for user: {}", userId);
            String url = aiServiceUrl + "/api/v1/analyze/personality/" + userId; // Doğru path

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", apiKey); // API Key header'ı eklendi
            headers.setContentType(MediaType.APPLICATION_JSON); // Content type belirtmek iyi pratik

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers); // POST için body yok, sadece header

            ResponseEntity<AnalysisResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST, // Metod POST olarak değiştirildi
                    requestEntity,
                    AnalysisResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully received analysis from AI service for user: {}", userId);
                return response.getBody();
            } else {
                // Hata durumunu daha detaylı logla
                log.error("AI service returned unsuccessful response: Status={}, Body={}", response.getStatusCode(), response.getBody());
                throw new AiServiceException("AI service returned unsuccessful response: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Error communicating with AI service for user {}: {}", userId, e.getMessage());
            // RestClientException'dan daha fazla detay alınabilir (örn. status code)
            String detail = (e instanceof HttpStatusCodeException) ?
                 ((HttpStatusCodeException)e).getResponseBodyAsString() : e.getMessage();
            throw new AiServiceException("Failed to communicate with AI service: " + detail, e);
        } catch (Exception e) {
            log.error("Unexpected error during AI service communication for user {}: {}", userId, e.getMessage());
            throw new AiServiceException("Unexpected error during AI service communication: " + e.getMessage(), e);
        }
    }
}
