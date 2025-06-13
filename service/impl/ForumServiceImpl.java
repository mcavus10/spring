package com.example.moodmovies.service.impl;

import com.example.moodmovies.dto.*;
import com.example.moodmovies.exception.ResourceNotFoundException;
import com.example.moodmovies.exception.UnauthorizedOperationException;
import com.example.moodmovies.exception.UserNotFoundException;
import com.example.moodmovies.model.ForumComment;
import com.example.moodmovies.model.ForumPost;
import com.example.moodmovies.model.User;
import com.example.moodmovies.repository.ForumCommentRepository;
import com.example.moodmovies.repository.ForumPostRepository;
import com.example.moodmovies.repository.UserRepository;
import com.example.moodmovies.service.ForumService;
import com.example.moodmovies.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements ForumService {

    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ForumPostDetailDTO createPost(String userId, ForumPostCreateDTO createDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));

        ForumPost post = ForumPost.builder()
                .user(user)
                .title(createDTO.getTitle())
                .context(createDTO.getContext())
                .tag(createDTO.getTag())
                .build();

        ForumPost savedPost = forumPostRepository.save(post);
        return mapToDetailDTO(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ForumPostSummaryDTO> getAllPosts(Pageable pageable) {
        Page<ForumPost> postPage = forumPostRepository.findAll(pageable);
        List<String> postIds = postPage.getContent().stream().map(ForumPost::getId).collect(Collectors.toList());

        // N+1 problemini çözmek için tüm yorum sayılarını tek bir sorguyla al
        Map<String, Long> commentCounts = Collections.emptyMap();
        if (!postIds.isEmpty()) {
            commentCounts = forumPostRepository.countCommentsByPostIds(postIds).stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],
                            row -> (Long) row[1]
                    ));
        }

        final Map<String, Long> finalCommentCounts = commentCounts;
        return postPage.map(post -> mapToSummaryDTO(post, finalCommentCounts.getOrDefault(post.getId(), 0L)));
    }

    @Override
    @Transactional(readOnly = true)
    public ForumPostDetailDTO getPostById(String postId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Forum başlığı bulunamadı: " + postId));
        return mapToDetailDTO(post);
    }

    @Override
    @Transactional
    public ForumCommentDTO addComment(String postId, String userId, ForumCommentCreateDTO commentDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Yorum yapılacak forum başlığı bulunamadı: " + postId));

        ForumComment comment = ForumComment.builder()
                .user(user)
                .forumPost(post)
                .comment(commentDTO.getComment())
                .build();

        ForumComment savedComment = forumCommentRepository.save(comment);
        return mapToCommentDTO(savedComment);
    }

    @Override
    @Transactional
    public void deletePost(String postId, String userId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Forum başlığı bulunamadı: " + postId));
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedOperationException("Bu başlığı silme yetkiniz yok.");
        }
        forumPostRepository.delete(post);
    }

    @Override
    @Transactional
    public void deleteComment(String commentId, String userId) {
        ForumComment comment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Yorum bulunamadı: " + commentId));
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedOperationException("Bu yorumu silme yetkiniz yok.");
        }
        forumCommentRepository.delete(comment);
    }
    
    // --- MAPPING HELPERS ---

    private ForumPostSummaryDTO mapToSummaryDTO(ForumPost post, long commentCount) {
        return ForumPostSummaryDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .tag(post.getTag())
                .author(userMapper.toUserSummaryDTO(post.getUser()))
                .commentCount(commentCount)
                .created(post.getCreated())
                .build();
    }

    private ForumCommentDTO mapToCommentDTO(ForumComment comment) {
        return ForumCommentDTO.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .author(userMapper.toUserSummaryDTO(comment.getUser()))
                .created(comment.getCreated())
                .lastUpd(comment.getLastUpd())
                .build();
    }

    private ForumPostDetailDTO mapToDetailDTO(ForumPost post) {
        List<ForumCommentDTO> commentDTOs = post.getComments().stream()
                .map(this::mapToCommentDTO)
                .collect(Collectors.toList());

        return ForumPostDetailDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .tag(post.getTag())
                .context(post.getContext())
                .author(userMapper.toUserSummaryDTO(post.getUser()))
                .created(post.getCreated())
                .lastUpd(post.getLastUpd())
                .comments(commentDTOs)
                .build();
    }
}