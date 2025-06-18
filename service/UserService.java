package com.example.moodmovies.service;

import java.util.List;
import java.util.Optional;
import com.example.moodmovies.dto.ProfileDataDTO;
import com.example.moodmovies.dto.UserDTO;
import com.example.moodmovies.dto.UserRegistrationRequestDTO;
import com.example.moodmovies.dto.UserUpdateRequestDTO;

public interface UserService {
    
    /**
     * Kullanıcıyı e-posta adresine göre bulur
     * 
     * @param email Aranacak e-posta adresi
     * @return E-postaya sahip kullanıcı bilgileri, kullanıcı yoksa boş Optional
     */
    Optional<UserDTO> findByEmail(String email);

    /**
     * Kullanıcıyı providerId değerine göre bulur (Google, Facebook vb. kullanıcılar için)
     * 
     * @param providerId Sağlayıcıdan gelen benzersiz kullanıcı ID'si
     * @return ProviderId'ye sahip kullanıcı bilgileri, kullanıcı yoksa boş Optional
     */
    Optional<UserDTO> findByProviderId(String providerId);

    /**
     * Kullanıcıyı ID'sine göre bulur
     * 
     * @param id Kullanıcı ID'si
     * @return Kullanıcı bilgileri, kullanıcı yoksa UserNotFoundException fırlatır
     */
    UserDTO findUserById(String id);

    /**
     * Yeni yerel kullanıcı kaydı yapar (e-posta/şifre ile kayıt)
     * 
     * @param registrationRequestDto Kayıt bilgileri
     * @return Kaydedilen kullanıcı bilgileri
     */
    UserDTO registerLocalUser(UserRegistrationRequestDTO registrationRequestDto);

    /**
     * OAuth2 ile giriş yapan kullanıcıyı işler
     * 
     * @param oauth2UserInfo OAuth2 sağlayıcısından gelen kullanıcı bilgileri
     * @return Kullanıcı bilgileri
     */
    UserDTO processOAuthUserLogin(OAuth2UserInfo oauth2UserInfo);

    /**
     * En çok film puanlayan kullanıcıları getirir
     * 
     * @param limit Dönülecek kullanıcı sayısı
     * @return En aktif kullanıcılar listesi
     */
    List<UserDTO> getTopReviewers(int limit);

    /**
     * Kullanıcının profil bilgilerini günceller
     * 
     * @param userId Güncellenecek kullanıcının ID'si
     * @param updateRequest Güncelleme bilgileri
     * @return Güncellenmiş kullanıcı bilgileri
     */
    UserDTO updateUserProfile(String userId, UserUpdateRequestDTO updateRequest);

    /**
     * Kullanıcının avatarını günceller
     * @param userId Kullanıcı ID'si
     * @param avatarId Yeni avatar ID'si
     * @return Güncellenmiş kullanıcı bilgileri
     */
    UserDTO updateUserAvatar(String userId, String avatarId);

    /**
     * Kullanıcının profil sayfasında gösterilecek tüm verileri toplar.
     * @param userId Kullanıcı ID'si
     * @return Profil verilerini içeren DTO
     */
    ProfileDataDTO getProfileData(String userId);
}
