package com.example.moodmovies.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.moodmovies.dto.UserDTO;
import com.example.moodmovies.dto.UserRegistrationRequestDTO;
import com.example.moodmovies.exception.UserNotFoundException;
import com.example.moodmovies.model.Authentication;
import com.example.moodmovies.model.AuthProvider;
import com.example.moodmovies.model.User;
import com.example.moodmovies.repository.AuthenticationRepository;
import com.example.moodmovies.repository.UserRepository;
import com.example.moodmovies.service.OAuth2UserInfo;
import com.example.moodmovies.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationRepository authenticationRepository;

    @Override
    @Transactional
    public Optional<UserDTO> findByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        return userOptional.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public Optional<UserDTO> findByProviderId(String providerId) {
        Optional<User> userOptional = userRepository.findByProviderId(providerId);
        return userOptional.map(this::convertToDTO);
    }
    
    @Override
    public UserDTO findUserById(String id) {
        User user = userRepository.findById(id)
        .orElseThrow(()-> new UserNotFoundException("User not found with id: "+id));

        return convertToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO registerLocalUser(UserRegistrationRequestDTO registrationRequestDto) {
        // Kullanıcı yerel kayıt işlemi
        User newUser = new User();
        newUser.setName(registrationRequestDto.getName()); // name alanını kullan
        newUser.setEmail(registrationRequestDto.getEmail());
        newUser.setPassword(registrationRequestDto.getPassword()); // Bu şifre zaten Controller'da hashlendi
        
        // LOCAL kimlik doğrulama sağlayıcısını veritabanından al
        Authentication localAuth = authenticationRepository.findById(AuthProvider.LOCAL)
                .orElseThrow(() -> new RuntimeException("LOCAL kimlik doğrulama sağlayıcısı bulunamadı"));
        
        newUser.setAuthentication(localAuth);
        newUser.setProviderId(null); // LOCAL için provider ID yok
        
        User savedUser = userRepository.save(newUser);
        return convertToDTO(savedUser);
    }

    @Override
    @Transactional
    public UserDTO processOAuthUserLogin(OAuth2UserInfo oauth2UserInfo) {
        // OAuth2 ile giriş yapan kullanıcıyı veritabanında ara
        return findByProviderId(oauth2UserInfo.getProviderId())
                .orElseGet(() -> {
                    // Kullanıcı daha önce giriş yapmamış, yeni kayıt oluştur
                    User newUser = new User();
                    newUser.setName(oauth2UserInfo.getName());
                    newUser.setEmail(oauth2UserInfo.getEmail());
                    
                    // Kimlik doğrulama sağlayıcısını veritabanından al (GOOGLE, FACEBOOK vb.)
                    String authProviderId = getAuthProviderId(oauth2UserInfo);
                    Authentication authProvider = authenticationRepository.findById(authProviderId)
                            .orElseThrow(() -> new RuntimeException(authProviderId + " kimlik doğrulama sağlayıcısı bulunamadı"));
                    
                    newUser.setAuthentication(authProvider);
                    newUser.setProviderId(oauth2UserInfo.getProviderId());
                    
                    User savedUser = userRepository.save(newUser);
                    return convertToDTO(savedUser);
                });
    }
    
    private String getAuthProviderId(OAuth2UserInfo oauth2UserInfo) {
        // OAuth2UserInfo'dan provider tipini belirle
        // Bu örnek fonksiyon, OAuth2UserInfo sınıfınızın yapısına göre düzenlenmelidir
        // Şu an varsayılan olarak GOOGLE dönüyor
        return AuthProvider.GOOGLE;
    }

    private UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .authType(user.getAuthentication().getId()) // Authentication tablosundan ID değerini al
                .createdDate(user.getCreatedDate())
                .lastUpdatedDate(user.getLastUpdatedDate())
                .build();
    }
}
