package com.example.moodmovies.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moodmovies.dto.AuthRequest;
import com.example.moodmovies.dto.AuthResponse;
import com.example.moodmovies.dto.UserDTO;
import com.example.moodmovies.dto.UserRegistrationRequestDTO;
import com.example.moodmovies.exception.EmailAlreadyExistsException;
import com.example.moodmovies.security.CookieService;
import com.example.moodmovies.security.JwtTokenProvider;
import com.example.moodmovies.security.UserPrincipal;
import com.example.moodmovies.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final CookieService cookieService;
    
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "API is working!");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest loginRequest) {
        // Kullanıcı kimlik doğrulama
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Token oluştur
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getId());

        // Kullanıcı bilgilerini al
        UserDTO userDTO = userService.findUserById(userPrincipal.getId());
        
        // Cookie'leri oluştur
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(accessToken).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(refreshToken).toString());

        // AuthResponse oluştur ve dön (artık token'ları içermez çünkü cookie olarak gönderildi)
        return ResponseEntity.ok()
                .headers(headers)
                .body(AuthResponse.builder()
                .tokenType("Bearer")
                .expiresInMs(86400000L) // 24 saat
                .user(userDTO)
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationRequestDTO registrationRequest) {
        // E-posta zaten kullanımda mı kontrol et
        if (userService.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Bu e-posta adresi zaten kullanımda: " + registrationRequest.getEmail());
        }

        // Şifreyi hashle
        registrationRequest.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        
        // LOCAL provider ile yeni kullanıcı oluştur
        UserDTO registeredUser = userService.registerLocalUser(registrationRequest);
        
        // Kayıt sonrası otomatik oturum açma için UserPrincipal oluştur
        UserPrincipal userPrincipal = UserPrincipal.builder()
            .id(registeredUser.getId())
            .email(registeredUser.getEmail())
            .name(registeredUser.getName()) 
            .password("") // Şifre alanı - doğrulama için gerekli değil
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
            .build();
        
        // Geçici kimlik doğrulama nesnesi oluştur
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, 
            null, // Credentials (bu aşamada null olabilir)
            userPrincipal.getAuthorities()
        );
        
        // Kimlik doğrulama bileşenini güvenlik bağlamına ayarla
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Token'ları oluştur
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(registeredUser.getId());
        
        // Cookie'leri oluştur
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(accessToken).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(refreshToken).toString());
        
        // AuthResponse oluştur ve dön
        return ResponseEntity.ok()
                .headers(headers)
                .body(AuthResponse.builder()
                .tokenType("Bearer")
                .expiresInMs(86400000L) // 24 saat
                .user(registeredUser)
                .build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenFromCookie) {
        
        String refreshToken = refreshTokenFromCookie;
        
        // Refresh token'ı doğrula
        if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().build();
        }

        // Token'dan user ID al
        String userId = tokenProvider.getUserIdFromJWT(refreshToken);

        // Yeni access token oluştur
        String newAccessToken = tokenProvider.generateTokenFromUserId(userId);
        String newRefreshToken = tokenProvider.generateRefreshToken(userId);

        // Kullanıcı bilgilerini al
        UserDTO userDTO = userService.findUserById(userId);
        
        // Cookie'leri oluştur
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(newAccessToken).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(newRefreshToken).toString());

        // AuthResponse oluştur ve dön
        return ResponseEntity.ok()
                .headers(headers)
                .body(AuthResponse.builder()
                .tokenType("Bearer")
                .expiresInMs(86400000L)
                .user(userDTO)
                .build());
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Güvenlik bağlamını temizle
        SecurityContextHolder.clearContext();
        
        // Cookie'leri sil
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieService.deleteAccessTokenCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieService.deleteRefreshTokenCookie().toString());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body("Başarıyla çıkış yapıldı");
    }
}
