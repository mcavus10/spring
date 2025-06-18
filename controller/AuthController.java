package com.example.moodmovies.controller;

import com.example.moodmovies.dto.AuthRequest;
import com.example.moodmovies.dto.AuthResponse;
import com.example.moodmovies.dto.UserDTO;
import com.example.moodmovies.dto.UserRegistrationRequestDTO;
import com.example.moodmovies.exception.EmailAlreadyExistsException;
import com.example.moodmovies.security.CookieService;
import com.example.moodmovies.security.JwtTokenProvider;
import com.example.moodmovies.security.UserPrincipal;
import com.example.moodmovies.service.OAuth2UserInfo;
import com.example.moodmovies.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j // Loglama için eklendi
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final CookieService cookieService;

    // Google Client ID'sini application.properties'ten alıyoruz
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

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
        log.info("Login isteği alındı: {}", loginRequest.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getId());

        UserDTO userDTO = userService.findUserById(userPrincipal.getId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(accessToken).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(refreshToken).toString());

        log.info("Kullanıcı {} için giriş başarılı.", userDTO.getEmail());
        return ResponseEntity.ok()
                .headers(headers)
                .body(AuthResponse.builder()
                        .tokenType("Bearer")
                        .expiresInMs(tokenProvider.getJwtExpirationMs())
                        .user(userDTO)
                        .accessToken(accessToken) // Mobil için
                        .build());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationRequestDTO registrationRequest) {
        log.info("Register isteği alındı: {}", registrationRequest.getEmail());
        if (userService.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Bu e-posta adresi zaten kullanımda: " + registrationRequest.getEmail());
        }

        registrationRequest.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        
        UserDTO registeredUser = userService.registerLocalUser(registrationRequest);
        
        UserPrincipal userPrincipal = UserPrincipal.builder()
            .id(registeredUser.getId())
            .email(registeredUser.getEmail())
            .name(registeredUser.getName())
            .password("") // Şifre kimlik doğrulama için gerekli değil
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
            .build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(registeredUser.getId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(accessToken).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(refreshToken).toString());
        
        log.info("Yeni kullanıcı kaydedildi ve oturum açıldı: {}", registeredUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED) // Yeni kaynak oluşturulduğu için 201 Created dönmek daha doğru
                .headers(headers)
                .body(AuthResponse.builder()
                        .tokenType("Bearer")
                        .expiresInMs(tokenProvider.getJwtExpirationMs())
                        .user(registeredUser)
                        .accessToken(accessToken) // Mobil için
                        .build());
    }

    /**
     * Android uygulamasından gelen Google ID Token'ını doğrular, kullanıcı oluşturur/günceller ve JWT döndürür.
     */
    @PostMapping("/google/verify")
    public ResponseEntity<AuthResponse> verifyGoogleToken(@RequestBody Map<String, String> tokenMap) {
        String idTokenString = tokenMap.get("idToken");
        log.info("Google token doğrulama isteği alındı.");
        if (idTokenString == null || idTokenString.isBlank()) {
            log.warn("Google token doğrulama isteği boş token ile geldi.");
            return ResponseEntity.badRequest().build();
        }

        // GoogleIdTokenVerifier'ı her istekte oluşturmak daha güvenli ve basittir.
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.warn("Geçersiz Google ID Token alındı.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String providerId = payload.getSubject();
            String pictureUrl = (String) payload.get("picture");

            log.info("Google token doğrulandı. Email: {}", email);

            // OAuth2UserServiceImpl'deki mantığı taklit ederek kullanıcıyı işle
            OAuth2UserInfo oauth2UserInfo = new OAuth2UserInfo(
                (Map<String, Object>) payload, providerId, name, email, pictureUrl
            );
            UserDTO userDTO = userService.processOAuthUserLogin(oauth2UserInfo);

            // Kullanıcı için kendi JWT token'ımızı oluştur
            String accessToken = tokenProvider.generateTokenFromUserId(userDTO.getId());
            
            log.info("Google kullanıcısı {} için JWT oluşturuldu.", userDTO.getEmail());

            // Mobil uygulama için AuthResponse'u döndür
            return ResponseEntity.ok(AuthResponse.builder()
                    .tokenType("Bearer")
                    .accessToken(accessToken)
                    .user(userDTO)
                    .expiresInMs(tokenProvider.getJwtExpirationMs())
                    .build());

        } catch (Exception e) {
            log.error("Google token doğrulama hatası: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshTokenFromCookie) {
        String refreshToken = refreshTokenFromCookie;
        if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().build();
        }

        String userId = tokenProvider.getUserIdFromJWT(refreshToken);
        String newAccessToken = tokenProvider.generateTokenFromUserId(userId);
        String newRefreshToken = tokenProvider.generateRefreshToken(userId);
        UserDTO userDTO = userService.findUserById(userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(newAccessToken).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(newRefreshToken).toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(AuthResponse.builder()
                        .tokenType("Bearer")
                        .expiresInMs(tokenProvider.getJwtExpirationMs())
                        .user(userDTO)
                        .accessToken(newAccessToken)
                        .build());
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookieService.deleteAccessTokenCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieService.deleteRefreshTokenCookie().toString());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body("Başarıyla çıkış yapıldı");
    }
}