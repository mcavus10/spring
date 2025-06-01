package com.example.moodmovies.controller;

import com.example.moodmovies.dto.AnalysisResponse;
import com.example.moodmovies.dto.ErrorDetail;
import com.example.moodmovies.dto.TestSubmissionRequestDto;
import com.example.moodmovies.exception.AiServiceException;
import com.example.moodmovies.exception.UserNotFoundException;
import com.example.moodmovies.security.UserPrincipal;
import com.example.moodmovies.service.PersonalityAnalysisService;
import com.example.moodmovies.service.TestSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling personality test submissions.
 */
@RestController
@RequestMapping("/api/v1/tests")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final TestSubmissionService testSubmissionService;
    private final PersonalityAnalysisService personalityAnalysisService;

    /**
     * Endpoint for submitting personality test answers.
     * 
     * @param currentUser The authenticated user (injected by Spring Security)
     * @param submissionRequest DTO containing the list of submitted answers
     * @return Response with success message or error details
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitTestAnswers(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody TestSubmissionRequestDto submissionRequest) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        if (submissionRequest == null || submissionRequest.answers() == null || submissionRequest.answers().isEmpty()) {
            return ResponseEntity.badRequest().body("No answers submitted");
        }

        try {
            // 1. Yanıtları kaydet
            testSubmissionService.saveUserResponses(currentUser.getId(), submissionRequest.answers());
            log.info("Test answers saved for user: {}", currentUser.getId());

            // 2. AI Analizini tetikle
            log.info("Triggering AI personality analysis for user: {}", currentUser.getId());
            AnalysisResponse analysisResult = personalityAnalysisService.analyzeUserPersonality(currentUser.getId());
            log.info("AI analysis completed for user: {}, Profile ID: {}", currentUser.getId(), analysisResult.getProfileId());

            // 3. AI'dan dönen sonucu Frontend'e döndür
            return ResponseEntity.ok(analysisResult);
            
        } catch (UserNotFoundException e) {
            log.warn("User not found during test submission/analysis: {}", currentUser.getId());
            ErrorDetail error = new ErrorDetail("Authenticated user not found in database.", "USER_NOT_FOUND", currentUser.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (AiServiceException e) {
            log.error("AI Service error during analysis for user {}: {}", currentUser.getId(), e.getMessage());
            ErrorDetail error = new ErrorDetail("AI service failed: " + e.getMessage(), "AI_SERVICE_ERROR", currentUser.getId());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        } catch (Exception e) {
            log.error("Error processing test submission for user {}: {}", currentUser.getId(), e.getMessage(), e);
            ErrorDetail error = new ErrorDetail("An internal error occurred while processing test results.", "INTERNAL_SERVER_ERROR", currentUser.getId());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
