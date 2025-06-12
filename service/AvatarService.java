package com.example.moodmovies.service;

import com.example.moodmovies.dto.AvatarDTO;
import com.example.moodmovies.model.Avatar;

import java.util.List;
import java.util.Optional;

public interface AvatarService {
    List<AvatarDTO> getAllAvatars();
    Optional<Avatar> getAvatarById(String avatarId);
    byte[] getAvatarImage(String avatarId);
    boolean isValidAvatarId(String avatarId);
} 