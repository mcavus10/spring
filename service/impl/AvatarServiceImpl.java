package com.example.moodmovies.service.impl;

import com.example.moodmovies.dto.AvatarDTO;
import com.example.moodmovies.model.Avatar;
import com.example.moodmovies.repository.AvatarRepository;
import com.example.moodmovies.service.AvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvatarServiceImpl implements AvatarService {
    private final AvatarRepository avatarRepository;

    @Override
    public List<AvatarDTO> getAllAvatars() {
        return avatarRepository.findAll().stream()
                .map(avatar -> AvatarDTO.builder()
                        .avatarId(avatar.getAvatarId())
                        .imageUrl("/api/v1/avatars/" + avatar.getAvatarId() + "/image")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Avatar> getAvatarById(String avatarId) {
        return avatarRepository.findById(avatarId);
    }

    @Override
    public byte[] getAvatarImage(String avatarId) {
        Avatar avatar = avatarRepository.findById(avatarId)
                .orElseThrow(() -> new IllegalArgumentException("Avatar bulunamadı: " + avatarId));
        return avatar.getImageByte();
    }

    @Override
    public boolean isValidAvatarId(String avatarId) {
        return avatarRepository.existsById(avatarId);
    }
} 