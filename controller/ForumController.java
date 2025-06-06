package com.example.moodmovies.controller;

import com.example.moodmovies.dto.*;
import com.example.moodmovies.security.UserPrincipal;
import com.example.moodmovies.service.ForumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @PostMapping("/posts")
    public ResponseEntity<ForumPostDetailDTO> createPost(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ForumPostCreateDTO createDTO) {
        ForumPostDetailDTO createdPost = forumService.createPost(currentUser.getId(), createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<ForumPostSummaryDTO>> getAllPosts(
            @PageableDefault(size = 20, sort = "created") Pageable pageable) {
        return ResponseEntity.ok(forumService.getAllPosts(pageable));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<ForumPostDetailDTO> getPostById(@PathVariable String postId) {
        return ResponseEntity.ok(forumService.getPostById(postId));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String postId) {
        forumService.deletePost(postId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ForumCommentDTO> addComment(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String postId,
            @Valid @RequestBody ForumCommentCreateDTO commentDTO) {
        ForumCommentDTO createdComment = forumService.addComment(postId, currentUser.getId(), commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String commentId) {
        forumService.deleteComment(commentId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}