package com.example.moodmovies.service.impl;

import com.example.moodmovies.dto.AnswerSubmissionDto;
import com.example.moodmovies.exception.UserNotFoundException;
import com.example.moodmovies.model.MoodmoviesResponse;
import com.example.moodmovies.model.User;
import com.example.moodmovies.repository.ResponseRepository;
import com.example.moodmovies.repository.UserRepository;
import com.example.moodmovies.service.TestSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of TestSubmissionService for handling personality test submissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestSubmissionServiceImpl implements TestSubmissionService {

    private final ResponseRepository responseRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void saveUserResponses(String userId, List<AnswerSubmissionDto> submittedAnswers) throws UserNotFoundException {
        // Find the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        log.info("Processing test responses for user: {}", userId);
        
        // 1. YENİ ADIM: Kullanıcının mevcut eski cevaplarını sil
        long deletedResponsesCount = responseRepository.deleteByUser_Id(userId);
        if (deletedResponsesCount > 0) {
            log.info("Deleted {} old test responses for user: {}", deletedResponsesCount, userId);
        } else {
            log.info("No old test responses found to delete for user: {}", userId);
        }
        
        // 2. Yeni cevapları oluştur ve kaydet
        log.info("Saving {} new test responses for user: {}", submittedAnswers.size(), userId);
        List<MoodmoviesResponse> newResponses = new ArrayList<>();
        for (AnswerSubmissionDto answerDto : submittedAnswers) {
            MoodmoviesResponse response = MoodmoviesResponse.builder()
                    // responseId Hibernate tarafından atanacak
                    .user(user)
                    .questionId(answerDto.questionId())
                    .answerId(answerDto.answerId())
                    // responseDate @CreationTimestamp ile atanacak
                    .build();
            
            newResponses.add(response);
        }
        
        // Save all responses to the database
        responseRepository.saveAll(newResponses);
        log.info("Successfully saved {} new test responses for user: {}", newResponses.size(), userId);
    }
}
