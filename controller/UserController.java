package com.example.moodmovies.controller;

import com.example.moodmovies.dto.UserDTO;
import com.example.moodmovies.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal; // Kimlik doğrulaması yapılmış kullanıcıyı almak için
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import com.example.moodmovies.security.UserPrincipal; // Kimlik doğrulaması yapılmış kullanıcı nesnesi


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
}
