package com.example.moodmovies.service;

import com.example.moodmovies.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ForumService {
    ForumPostDetailDTO createPost(String userId, ForumPostCreateDTO createDTO);
    Page<ForumPostSummaryDTO> getAllPosts(Pageable pageable);
    ForumPostDetailDTO getPostById(String postId);
    ForumCommentDTO addComment(String postId, String userId, ForumCommentCreateDTO commentDTO);
    void deletePost(String postId, String userId);
    void deleteComment(String commentId, String userId);
}