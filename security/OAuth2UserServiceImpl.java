package com.example.moodmovies.security;

import java.util.Map;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.moodmovies.exception.OAuth2AuthenticationProcessingException;
import com.example.moodmovies.model.Authentication;
import com.example.moodmovies.model.AuthProvider;
import com.example.moodmovies.model.User;
import com.example.moodmovies.repository.AuthenticationRepository;
import com.example.moodmovies.repository.UserRepository;
import com.example.moodmovies.service.oauth2.FacebookOAuth2UserInfo;
import com.example.moodmovies.service.OAuth2UserInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuthenticationRepository authenticationRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        // Provider bilgisini al (google, facebook vb)
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 login with provider: {}", registrationId);
        
        OAuth2UserInfo oAuth2UserInfo;
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // Farklı provider'lara göre bilgileri çıkarma
        if ("google".equalsIgnoreCase(registrationId)) {
            oAuth2UserInfo = new OAuth2UserInfo(
                attributes,
                (String) attributes.get("sub"),
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                (String) attributes.get("picture")
            );
        } else if ("facebook".equalsIgnoreCase(registrationId)) {
            // Facebook için özel sınıfımızı kullanarak bilgileri işle
            oAuth2UserInfo = FacebookOAuth2UserInfo.extract(attributes);
            log.info("Facebook user info processed: {}", oAuth2UserInfo.getEmail());
        } else {
            log.error("Login with {} OAuth2 provider is not supported yet", registrationId);
            throw new OAuth2AuthenticationProcessingException(
                    "Bu kimlik sağlayıcı (" + registrationId + ") desteklenmiyor.");
        }

        // E-posta doğrulama
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email bulunamadı");
        }

        log.info("Processing OAuth2 user: {}", oAuth2UserInfo.getEmail());

        // Kimlik doğrulama provider'ını veritabanından al
        Authentication authProvider = getAuthenticationProvider(registrationId);
        
        // Kullanıcıyı e-posta ile kontrol et
        User user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
                .map(existingUser -> {
                    // Kullanıcı varsa, provider tipini kontrol et
                    if (!existingUser.getAuthentication().getId().equals(authProvider.getId())) {
                        throw new OAuth2AuthenticationProcessingException(
                                "Bu e-posta zaten " + existingUser.getAuthentication().getName() + " ile kayıtlı. "
                                        + "Lütfen " + existingUser.getAuthentication().getName() + " hesabınızı kullanarak giriş yapın.");
                    }
                    // Mevcut Google kullanıcısını güncelle
                    updateExistingUser(existingUser, oAuth2UserInfo);
                    log.info("Updated existing user: {}", existingUser.getEmail());
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    // Yeni kullanıcı oluştur
                    User newUser = new User();
                    newUser.setAuthentication(authProvider);
                    newUser.setProviderId(oAuth2UserInfo.getProviderId());
                    newUser.setName(oAuth2UserInfo.getName());
                    newUser.setEmail(oAuth2UserInfo.getEmail());
                    // Varsayılan avatar ata
                    newUser.setAvatarId("0000-000001-AVT"); // Varsayılan avatar ID'si
                    
                    log.info("Created new user with OAuth2: {}", newUser.getEmail());
                    User savedUser = userRepository.save(newUser);
                    return savedUser;
                });

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private void updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setName(oAuth2UserInfo.getName());
        // Diğer güncellenecek bilgiler varsa burada ekle
    }
    
    private Authentication getAuthenticationProvider(String registrationId) {
        String authId;
        
        if ("google".equalsIgnoreCase(registrationId)) {
            authId = AuthProvider.GOOGLE;
        } else if ("facebook".equalsIgnoreCase(registrationId)) {
            authId = AuthProvider.FACEBOOK;
        } else {
            authId = AuthProvider.LOCAL; // Varsayılan
        }
        
        return authenticationRepository.findById(authId)
                .orElseThrow(() -> new OAuth2AuthenticationProcessingException("Kimlik doğrulama sağlayıcı bulunamadı: " + authId));
    }
}
