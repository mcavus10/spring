package com.example.moodmovies.service.mapper;

import com.example.moodmovies.dto.UserDTO;
import com.example.moodmovies.dto.UserSummaryDTO;
import com.example.moodmovies.model.User;
import com.example.moodmovies.service.AvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class UserMapper {

    @SuppressWarnings("unused")
    private final AvatarService avatarService;

    // User -> UserDTO dönüşümü
    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        String avatarUrl = getAvatarFullUrl(user.getAvatarId());

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .authType(user.getAuthentication() != null ? user.getAuthentication().getId() : null)
                .providerId(user.getProviderId())
                .createdDate(user.getCreatedDate())
                .lastUpdatedDate(user.getLastUpdatedDate())
                .ratingCount(user.getRatingCount())
                .favoriteCount(user.getFavoriteCount())
                .listCount(user.getListCount())
                .avatarId(user.getAvatarId())
                .avatarImageUrl(avatarUrl)
                .build();
    }

    // User -> UserSummaryDTO dönüşümü
    public UserSummaryDTO toUserSummaryDTO(User user) {
        if (user == null) {
            return null;
        }

        String avatarUrl = getAvatarFullUrl(user.getAvatarId());

        return UserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .avatarImageUrl(avatarUrl)
                .build();
    }

    // Ortak avatar URL oluşturma metodu
    private String getAvatarFullUrl(String avatarId) {
        if (avatarId == null) {
            return null;
        }
        try {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return baseUrl + "/api/v1/avatars/" + avatarId + "/image";
        } catch (Exception e) {
            // Fallback for when not in a request context
            return "http://localhost:8080/api/v1/avatars/" + avatarId + "/image";
        }
    }
}