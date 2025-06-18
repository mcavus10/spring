package com.example.moodmovies.service.impl;

import com.example.moodmovies.dto.*;
import com.example.moodmovies.exception.UserNotFoundException;
import com.example.moodmovies.model.Authentication;
import com.example.moodmovies.model.AuthProvider;
import com.example.moodmovies.model.User;
import com.example.moodmovies.repository.AuthenticationRepository;
import com.example.moodmovies.repository.FilmListRepository;
import com.example.moodmovies.repository.FilmPointRepository;
import com.example.moodmovies.repository.UserRepository;
import com.example.moodmovies.service.AvatarService;
import com.example.moodmovies.service.OAuth2UserInfo;
import com.example.moodmovies.service.UserInteractionService;
import com.example.moodmovies.service.UserService;
import com.example.moodmovies.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationRepository authenticationRepository;
    private final FilmPointRepository filmPointRepository;
    private final FilmListRepository filmListRepository;
    private final AvatarService avatarService;
    private final UserMapper userMapper;
    private final UserInteractionService userInteractionService; // Profil verilerini toplamak için eklendi

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toUserDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId).map(userMapper::toUserDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        List<String> userIds = Collections.singletonList(id);
        Long ratingCnt = filmPointRepository.countRatingsByUserIds(userIds).stream()
                .findFirst().map(row -> ((Number) row[1]).longValue()).orElse(0L);
        Long favoriteCnt = filmPointRepository.countFavoritesByUserIds(userIds).stream()
                .findFirst().map(row -> ((Number) row[1]).longValue()).orElse(0L);
        Long listCnt = filmListRepository.countListsByUserIds(userIds).stream()
                .findFirst().map(row -> ((Number) row[1]).longValue()).orElse(0L);

        user.setRatingCount(ratingCnt);
        user.setFavoriteCount(favoriteCnt);
        user.setListCount(listCnt);

        return userMapper.toUserDTO(user);
    }

    @Override
    @Transactional
    public UserDTO registerLocalUser(UserRegistrationRequestDTO registrationRequestDto) {
        User newUser = new User();
        newUser.setName(registrationRequestDto.getName());
        newUser.setEmail(registrationRequestDto.getEmail());
        newUser.setPassword(registrationRequestDto.getPassword());

        Authentication localAuth = authenticationRepository.findById(AuthProvider.LOCAL)
                .orElseThrow(() -> new RuntimeException("LOCAL kimlik doğrulama sağlayıcısı bulunamadı"));

        newUser.setAuthentication(localAuth);
        newUser.setProviderId(null);
        newUser.setAvatarId("0000-000001-AVT"); // Varsayılan avatar ID'si

        User savedUser = userRepository.save(newUser);
        return userMapper.toUserDTO(savedUser);
    }

    @Override
    @Transactional
    public UserDTO processOAuthUserLogin(OAuth2UserInfo oauth2UserInfo) {
        // Bu metot, OAuth2UserServiceImpl tarafından kullanıldığı için buradaki implementasyon
        // genellikle daha basit kalabilir veya tamamen oraya taşınabilir.
        // Şimdilik mevcut mantığı koruyoruz.
        return findByProviderId(oauth2UserInfo.getProviderId())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName(oauth2UserInfo.getName());
                    newUser.setEmail(oauth2UserInfo.getEmail());

                    String authProviderId = getAuthProviderId(oauth2UserInfo);
                    Authentication authProvider = authenticationRepository.findById(authProviderId)
                            .orElseThrow(() -> new RuntimeException(authProviderId + " kimlik doğrulama sağlayıcısı bulunamadı"));

                    newUser.setAuthentication(authProvider);
                    newUser.setProviderId(oauth2UserInfo.getProviderId());
                    newUser.setAvatarId("0000-000001-AVT");

                    User savedUser = userRepository.save(newUser);
                    return userMapper.toUserDTO(savedUser);
                });
    }
    
    /**
     * YENİ METOT: Kullanıcının profil sayfası için gerekli tüm verileri toplar.
     * @param userId İstenen kullanıcının ID'si
     * @return Profil verilerini içeren DTO
     */
    @Override
    @Transactional(readOnly = true)
    public ProfileDataDTO getProfileData(String userId) {
        // 1. Temel kullanıcı bilgilerini ve istatistiklerini al
        UserDTO userDTO = findUserById(userId);

        // 2. Favori filmleri al
        List<FilmSummaryDTO> favorites = userInteractionService.getUserFavoriteFilms(userId);

        // 3. Kullanıcının listelerini al (sadece aktif olanlar)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));

        List<FilmListSummaryDTO> lists = filmListRepository.findAllByUserAndStatus(user, 1) // 1 = Aktif
                .stream()
                .map(filmList -> {
                    // Listedeki ilk filmin posterini kapak resmi olarak kullan
                    List<FilmSummaryDTO> filmPreviews = filmList.getFilmListInfos().stream()
                            .limit(4) // Kolaj için ilk 4 resmi al
                            .map(info -> FilmSummaryDTO.builder()
                                    .id(info.getId().getFilmId())
                                    .title("") // Gerekli değil
                                    .imageUrl("http://10.0.2.2:8080/api/v1/films/image/" + info.getId().getFilmId())
                                    .build())
                            .collect(Collectors.toList());

                    return FilmListSummaryDTO.builder()
                            .listId(filmList.getListId())
                            .name(filmList.getName())
                            .tag(filmList.getTag())
                            .filmCount(filmList.getFilmListInfos().size())
                            .visibility(filmList.getVisible())
                            .owner(userMapper.toUserSummaryDTO(filmList.getUser()))
                            .films(filmPreviews) // Android tarafında kapak resmi oluşturmak için
                            .build();
                })
                .collect(Collectors.toList());

        // 4. Son puanlamaları al (örneğin son 10)
        List<RatedFilmDTO> ratings = userInteractionService.getLatestRatedFilms(userId, 10);

        // 5. Tüm veriyi tek bir DTO'da birleştir
        return ProfileDataDTO.builder()
                .user(userDTO)
                .favorites(favorites)
                .lists(lists)
                .ratings(ratings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getTopReviewers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        List<User> topUsers = userRepository.findTopReviewers(pageable).stream()
                .map(result -> (User) result[0])
                .collect(Collectors.toList());

        if (topUsers.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> userIds = topUsers.stream().map(User::getId).collect(Collectors.toList());

        Map<String, Long> ratingCounts = filmPointRepository.countRatingsByUserIds(userIds).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
        Map<String, Long> favoriteCounts = filmPointRepository.countFavoritesByUserIds(userIds).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
        Map<String, Long> listCounts = filmListRepository.countListsByUserIds(userIds).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));

        return topUsers.stream().map(user -> {
            UserDTO dto = userMapper.toUserDTO(user);
            dto.setRatingCount(ratingCounts.getOrDefault(user.getId(), 0L));
            dto.setFavoriteCount(favoriteCounts.getOrDefault(user.getId(), 0L));
            dto.setListCount(listCounts.getOrDefault(user.getId(), 0L));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUserProfile(String userId, UserUpdateRequestDTO updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setName(updateRequest.getName());
        User updatedUser = userRepository.save(user);
        return findUserById(updatedUser.getId()); // İstatistiklerle birlikte döndür
    }

    @Override
    @Transactional
    public UserDTO updateUserAvatar(String userId, String avatarId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (!avatarService.isValidAvatarId(avatarId)) {
            throw new IllegalArgumentException("Geçersiz avatar ID: " + avatarId);
        }

        user.setAvatarId(avatarId);
        User updatedUser = userRepository.save(user);
        return findUserById(updatedUser.getId()); // İstatistiklerle birlikte döndür
    }

    private String getAuthProviderId(OAuth2UserInfo oauth2UserInfo) {
        // Bu metot, OAuth2UserInfo'nun yapısına göre hangi provider olduğunu belirlemeli.
        // Şimdilik varsayılan olarak GOOGLE dönüyor, çünkü Facebook için de benzer bir yapı var.
        // registrationId'yi OAuth2UserServiceImpl'den buraya taşımak daha doğru bir pattern olabilir.
        return AuthProvider.GOOGLE;
    }
}