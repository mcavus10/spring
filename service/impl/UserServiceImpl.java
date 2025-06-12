package com.example.moodmovies.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moodmovies.dto.UserDTO;
import com.example.moodmovies.dto.UserRegistrationRequestDTO;
import com.example.moodmovies.dto.UserUpdateRequestDTO;
import com.example.moodmovies.exception.UserNotFoundException;
import com.example.moodmovies.model.Authentication;
import com.example.moodmovies.model.AuthProvider;
import com.example.moodmovies.model.User;
import com.example.moodmovies.repository.AuthenticationRepository;
import com.example.moodmovies.repository.FilmListRepository;
import com.example.moodmovies.repository.FilmPointRepository;
import com.example.moodmovies.repository.UserRepository;
import com.example.moodmovies.service.OAuth2UserInfo;
import com.example.moodmovies.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationRepository authenticationRepository;
    private final FilmPointRepository filmPointRepository; // YENİ EKLENDİ
    private final FilmListRepository filmListRepository; // YENİ EKLENDİ

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

    // YERİNE BU METODU YAPIŞTIR
@Override
@Transactional(readOnly = true)
public List<UserDTO> getTopReviewers(int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    
    // 1. En aktif kullanıcıları bul
    List<User> topUsers = userRepository.findTopReviewers(pageable).stream()
            .map(result -> (User) result[0])
            .collect(Collectors.toList());

    if (topUsers.isEmpty()) {
        return Collections.emptyList();
    }

    List<String> userIds = topUsers.stream().map(User::getId).collect(Collectors.toList());

    // 2. Bu kullanıcıların tüm istatistiklerini tek seferde ve verimli bir şekilde çek
    Map<String, Long> ratingCounts = filmPointRepository.countRatingsByUserIds(userIds).stream()
            .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
    
    Map<String, Long> favoriteCounts = filmPointRepository.countFavoritesByUserIds(userIds).stream()
            .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));

    Map<String, Long> listCounts = filmListRepository.countListsByUserIds(userIds).stream()
            .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));

    // 3. User nesnelerini istatistiklerle zenginleştirilmiş UserDTO'lara dönüştür
    return topUsers.stream().map(user -> {
        UserDTO dto = convertToDTO(user); // Önce temel DTO oluşturulur
        dto.setRatingCount(ratingCounts.getOrDefault(user.getId(), 0L));
        dto.setFavoriteCount(favoriteCounts.getOrDefault(user.getId(), 0L));
        dto.setListCount(listCounts.getOrDefault(user.getId(), 0L));
        return dto;
    }).collect(Collectors.toList());
}
    
    @Override
    @Transactional
    public UserDTO updateUserProfile(String userId, UserUpdateRequestDTO updateRequest) {
        // Kullanıcıyı bul
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        // İsmi güncelle
        user.setName(updateRequest.getName());
        
        // Kullanıcıyı kaydet ve güncellenmiş DTO döndür
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
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
