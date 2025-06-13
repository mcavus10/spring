package com.example.moodmovies.service.mapper;

import com.example.moodmovies.dto.UserDTO;
import com.example.moodmovies.dto.UserSummaryDTO;
import com.example.moodmovies.model.Avatar;
import com.example.moodmovies.model.User;
import com.example.moodmovies.service.AvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final AvatarService avatarService;

    // User -> UserDTO dönüşümü
    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        String avatarDataUrl = getAvatarDataUrl(user.getAvatarId());

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
                .avatarImageUrl(avatarDataUrl)
                .build();
    }

    // User -> UserSummaryDTO dönüşümü
    public UserSummaryDTO toUserSummaryDTO(User user) {
        if (user == null) {
            return null;
        }

        String avatarDataUrl = getAvatarDataUrl(user.getAvatarId());

        return UserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .avatarImageUrl(avatarDataUrl)
                .build();
    }

    // Ortak avatar URL oluşturma metodu
    private String getAvatarDataUrl(String avatarId) {
        if (avatarId == null) {
            return null;
        }
        Optional<Avatar> avatarOpt = avatarService.getAvatarById(avatarId);
        if (avatarOpt.isPresent()) {
            byte[] imageBytes = avatarOpt.get().getImageByte();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            return "data:image/jpeg;base64," + base64Image;
        }
        return null;
    }
} 