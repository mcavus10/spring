package com.example.moodmovies.controller;

import com.example.moodmovies.dto.AvatarUpdateRequestDTO;
import com.example.moodmovies.dto.ProfileDataDTO; // Yeni import
import com.example.moodmovies.dto.UserDTO;
import com.example.moodmovies.dto.UserUpdateRequestDTO;
import com.example.moodmovies.security.UserPrincipal;
import com.example.moodmovies.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        UserDTO userDTO = userService.findUserById(id);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Sadece temel kullanıcı bilgilerini (istatistikler dahil) getirir.
     * @param userPrincipal Spring Security tarafından sağlanan kimlik doğrulaması yapılmış kullanıcı
     * @return Kullanıcı bilgileri veya 401 Unauthorized hatası
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDTO userDTO = userService.findUserById(userPrincipal.getId());
        return ResponseEntity.ok(userDTO);
    }

    /**
     * YENİ ENDPOINT: Android profil sayfası için gerekli tüm verileri (kullanıcı, favoriler, listeler, puanlar) döndürür.
     * @param userPrincipal Spring Security tarafından sağlanan kimlik doğrulaması yapılmış kullanıcı
     * @return Profil verilerini içeren DTO veya 401 Unauthorized hatası
     */
    @GetMapping("/me/profile-data")
    public ResponseEntity<ProfileDataDTO> getCurrentUserProfileData(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ProfileDataDTO profileData = userService.getProfileData(userPrincipal.getId());
        return ResponseEntity.ok(profileData);
    }

    /**
     * En çok film puanlayan kullanıcıları getirir.
     */
    @GetMapping("/popular/reviewers")
    public ResponseEntity<List<UserDTO>> getTopReviewers(@RequestParam(defaultValue = "2") int limit) {
        return ResponseEntity.ok(userService.getTopReviewers(limit));
    }

    /**
     * Kullanıcının profil bilgilerini günceller.
     * @param id Güncellenecek kullanıcının ID'si
     * @param updateRequest Güncelleme bilgileri
     * @return Güncellenmiş kullanıcı bilgileri
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUserProfile(@PathVariable String id,
                                                    @Valid @RequestBody UserUpdateRequestDTO updateRequest) {
        // Not: Güvenlik açısından burada `id`'nin giriş yapmış kullanıcıya ait olup olmadığı kontrol edilmelidir.
        // Örnek: if (!userPrincipal.getId().equals(id)) { throw new Unauthorized... }
        UserDTO updatedUserDTO = userService.updateUserProfile(id, updateRequest);
        return ResponseEntity.ok(updatedUserDTO);
    }

    /**
     * Kullanıcının avatarını günceller.
     * @param userPrincipal Kimlik doğrulaması yapılmış kullanıcı
     * @param updateRequest Avatar güncelleme isteği
     * @return Güncellenmiş kullanıcı bilgileri
     */
    @PutMapping("/me/avatar")
    public ResponseEntity<UserDTO> updateUserAvatar(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                   @Valid @RequestBody AvatarUpdateRequestDTO updateRequest) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDTO updatedUserDTO = userService.updateUserAvatar(userPrincipal.getId(), updateRequest.getAvatarId());
        return ResponseEntity.ok(updatedUserDTO);
    }
}