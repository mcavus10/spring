package com.example.moodmovies.controller;

import com.example.moodmovies.dto.UserDTO;
import com.example.moodmovies.dto.UserUpdateRequestDTO;
import com.example.moodmovies.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Kimlik doğrulaması yapılmış kullanıcıyı almak için
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import com.example.moodmovies.security.UserPrincipal; // Kimlik doğrulaması yapılmış kullanıcı nesnesi
import com.example.moodmovies.dto.AvatarUpdateRequestDTO;


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
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email ){
        //getuserbyemail optional değer döndürdüğü için sonucu kontrol ediyoruz 
        return userService.findByEmail(email)
        .map(userDTO -> ResponseEntity.ok(userDTO))
        .orElseGet(()-> ResponseEntity.notFound().build());
    }

    
    /**
     * Giriş yapmış kullanıcının bilgilerini getirir (JWT veya OAuth2 ile doğrulanmış)
     * @param userPrincipal Spring Security tarafından sağlanan kimlik doğrulaması yapılmış kullanıcı
     * @return Kullanıcı bilgileri veya 401 Unauthorized hatası
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // Kullanıcı kimlik doğrulaması yapılmamışsa 401 Unauthorized hatası döndür
        if (userPrincipal == null) {
            // Bu durum normalde JwtAuthenticationFilter veya OAuth2 akışı
            // tarafından engellenmeli, ama bir güvenlik kontrolü olarak ekliyoruz
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // UserPrincipal'dan alınan ID ile tam UserDTO bilgisini servisten çek
            UserDTO userDTO = userService.findUserById(userPrincipal.getId());
            return ResponseEntity.ok(userDTO); 
        } catch (Exception ex) {
            // Kullanıcı bulunamazsa veya başka bir hata oluşursa 401 hatası döndür
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * En çok film puanlayan kullanıcıları getirir
     */
    @GetMapping("/popular/reviewers")
    public ResponseEntity<List<UserDTO>> getTopReviewers(@RequestParam(defaultValue = "2") int limit) {
        return ResponseEntity.ok(userService.getTopReviewers(limit));
    }

    /**
     * Kullanıcının profil bilgilerini günceller
     * @param id Güncellenecek kullanıcının ID'si
     * @param updateRequest Güncelleme bilgileri
     * @return Güncellenmiş kullanıcı bilgileri
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUserProfile(@PathVariable String id, 
                                                    @Valid @RequestBody UserUpdateRequestDTO updateRequest) {
        UserDTO updatedUserDTO = userService.updateUserProfile(id, updateRequest);
        return ResponseEntity.ok(updatedUserDTO);
    }

    /**
     * Kullanıcının avatarını günceller
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
        try {
            UserDTO updatedUserDTO = userService.updateUserAvatar(userPrincipal.getId(), updateRequest.getAvatarId());
            return ResponseEntity.ok(updatedUserDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
