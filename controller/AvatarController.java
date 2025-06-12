package com.example.moodmovies.controller;

import com.example.moodmovies.dto.AvatarDTO;
import com.example.moodmovies.service.AvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/avatars")
@RequiredArgsConstructor
public class AvatarController {
    private final AvatarService avatarService;

    @GetMapping
    public ResponseEntity<List<AvatarDTO>> getAllAvatars() {
        List<AvatarDTO> avatars = avatarService.getAllAvatars();
        return ResponseEntity.ok(avatars);
    }

    @GetMapping("/{avatarId}/image")
    public ResponseEntity<byte[]> getAvatarImage(@PathVariable String avatarId) {
        try {
            byte[] image = avatarService.getAvatarImage(avatarId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Varsayılan olarak JPEG, gerekirse dinamikleştir
            headers.setCacheControl("public, max-age=86400"); // 1 gün cache
            return new ResponseEntity<>(image, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
} 