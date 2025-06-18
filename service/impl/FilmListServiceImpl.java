package com.example.moodmovies.service.impl;

import com.example.moodmovies.dto.*;
import com.example.moodmovies.exception.FilmNotFoundException;
import com.example.moodmovies.exception.ResourceNotFoundException;
import com.example.moodmovies.exception.UnauthorizedOperationException;
import com.example.moodmovies.exception.UserNotFoundException;
import com.example.moodmovies.model.*;
import com.example.moodmovies.repository.FilmInfoRepository;
import com.example.moodmovies.repository.FilmListInfoRepository;
import com.example.moodmovies.repository.FilmListRepository;
import com.example.moodmovies.repository.UserRepository;
import com.example.moodmovies.service.FilmListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable; // DOĞRU IMPORT BU
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FilmListServiceImpl implements FilmListService {

    private final UserRepository userRepository;
    private final FilmListRepository filmListRepository;
    private final FilmListInfoRepository filmListInfoRepository;
    private final FilmInfoRepository filmInfoRepository;

    // Veritabanındaki VISIBLE INT değerlerine karşılık gelen sabitler
    // Not: Bunları bir Enum sınıfında tanımlayıp JPA AttributeConverter ile map etmek daha iyi bir pratiktir.
    private static final Integer VISIBILITY_PUBLIC = 1;  // Örnek: 1 Herkese Açık
    private static final Integer VISIBILITY_PRIVATE = 0; // Örnek: 0 Sadece Ben

    private static final Integer STATUS_ACTIVE = 1; // Yeni listeler için varsayılan aktif durumu

    @Override
    public FilmListDetailDTO createList(String userId, ListCreateRequestDTO createRequestDTO) {
        log.debug("Liste oluşturma işlemi başlatıldı - UserId: {}, ListName: {}", userId, createRequestDTO.getName());
        long startTime = System.currentTimeMillis();
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Liste oluşturma başarısız - Kullanıcı bulunamadı: {}", userId);
                        return new UserNotFoundException("Kullanıcı bulunamadı: " + userId);
                    });

            log.debug("Kullanıcı bulundu - UserId: {}, UserName: {}", userId, user.getName());

            FilmList filmList = FilmList.builder()
                    .user(user)
                    .name(createRequestDTO.getName())
                    .description(createRequestDTO.getDescription())
                    .tag(createRequestDTO.getTag()) // Veritabanında TAG NOT NULL olduğu için DTO'da da zorunlu olmalı
                    .visible(createRequestDTO.getVisible())
                    .status(STATUS_ACTIVE) // Yeni listeler varsayılan olarak aktif
                    .build();

            FilmList savedList = filmListRepository.save(filmList);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Liste başarıyla oluşturuldu - UserId: {}, ListId: {}, ListName: '{}', Tag: '{}', Visibility: {}, Duration: {}ms", 
                    userId, savedList.getListId(), savedList.getName(), savedList.getTag(), savedList.getVisible(), duration);
            
            return mapToFilmListDetailDTO(savedList);
            
        } catch (UserNotFoundException e) {
            throw e; // Zaten loglandı
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Liste oluşturma hatası - UserId: {}, ListName: '{}', Error: {}, Duration: {}ms", 
                    userId, createRequestDTO.getName(), e.getMessage(), duration, e);
            throw new RuntimeException("Liste oluşturma sırasında beklenmeyen bir hata oluştu", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilmListSummaryDTO> getUserLists(String userId, boolean includePrivate) {
        log.debug("Kullanıcı listeleri getiriliyor - UserId: {}, IncludePrivate: {}", userId, includePrivate);
        long startTime = System.currentTimeMillis();
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Kullanıcı listeleri getirme başarısız - Kullanıcı bulunamadı: {}", userId);
                        return new UserNotFoundException("Kullanıcı bulunamadı: " + userId);
                    });

            List<FilmList> lists;
            if (includePrivate) { // Eğer kullanıcı kendi listelerini istiyorsa (özel olanlar dahil)
                lists = filmListRepository.findAllByUserAndStatus(user, STATUS_ACTIVE);
                log.debug("Kullanıcının tüm listeleri (private dahil) getirildi - UserId: {}, Count: {}", userId, lists.size());
            } else { // Başka bir kullanıcının listelerini görüyorsak, sadece aktif VE public olanlar
                lists = filmListRepository.findAllByUserAndVisibleAndStatus(user, VISIBILITY_PUBLIC, STATUS_ACTIVE);
                log.debug("Kullanıcının public listeleri getirildi - UserId: {}, Count: {}", userId, lists.size());
            }
            
            List<FilmListSummaryDTO> result = lists.stream()
                    .map(this::mapToFilmListSummaryDTO)
                    .collect(Collectors.toList());
                    
            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Kullanıcı listeleri başarıyla getirildi - UserId: {}, Count: {}, IncludePrivate: {}, Duration: {}ms", 
                    userId, result.size(), includePrivate, duration);
            
            return result;
            
        } catch (UserNotFoundException e) {
            throw e; // Zaten loglandı
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Kullanıcı listeleri getirme hatası - UserId: {}, IncludePrivate: {}, Error: {}, Duration: {}ms", 
                    userId, includePrivate, e.getMessage(), duration, e);
            throw new RuntimeException("Kullanıcı listeleri getirilirken beklenmeyen bir hata oluştu", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FilmListDetailDTO getListDetails(String listId, String currentUserId) {
        log.debug("Liste detayları getiriliyor - ListId: {}, CurrentUserId: {}", listId, currentUserId);
        long startTime = System.currentTimeMillis();
        
        try {
            FilmList filmList = filmListRepository.findById(listId)
                    .orElseThrow(() -> {
                        log.warn("❌ Liste detayları getirme başarısız - Liste bulunamadı: {}", listId);
                        return new ResourceNotFoundException("Liste bulunamadı: " + listId);
                    });

            log.debug("Liste bulundu - ListId: {}, Name: '{}', Owner: {}, Visibility: {}", 
                    listId, filmList.getName(), filmList.getUser().getId(), filmList.getVisible());

            // Eğer liste özel ise (private) ve bakan kişi (currentUserId) listenin sahibi değilse, yetki hatası ver.
            // currentUserId null ise (anonim kullanıcı), özel listeleri göremez.
            if (filmList.getVisible().equals(VISIBILITY_PRIVATE) &&
                (currentUserId == null || !filmList.getUser().getId().equals(currentUserId))) {
                log.warn("❌ Yetkisiz liste erişimi - ListId: {}, Owner: {}, CurrentUser: {}, Visibility: Private", 
                        listId, filmList.getUser().getId(), currentUserId);
                throw new UnauthorizedOperationException("Bu özel listeyi görüntüleme yetkiniz yok.");
            }
            
            FilmListDetailDTO result = mapToFilmListDetailDTO(filmList);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Liste detayları başarıyla getirildi - ListId: {}, FilmCount: {}, CurrentUserId: {}, Duration: {}ms", 
                    listId, result.getFilms().size(), currentUserId, duration);
            
            return result;
            
        } catch (ResourceNotFoundException | UnauthorizedOperationException e) {
            throw e; // Zaten loglandı
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Liste detayları getirme hatası - ListId: {}, CurrentUserId: {}, Error: {}, Duration: {}ms", 
                    listId, currentUserId, e.getMessage(), duration, e);
            throw new RuntimeException("Liste detayları getirilirken beklenmeyen bir hata oluştu", e);
        }
    }

    @Override
    public FilmListDetailDTO updateList(String userId, String listId, ListUpdateRequestDTO updateRequestDTO) {
        log.debug("Liste güncelleme işlemi başlatıldı - UserId: {}, ListId: {}", userId, listId);
        long startTime = System.currentTimeMillis();
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Liste güncelleme başarısız - Kullanıcı bulunamadı: {}", userId);
                        return new UserNotFoundException("Kullanıcı bulunamadı: " + userId);
                    });
                    
            // Kullanıcının sadece kendi listesini güncelleyebilmesini sağla
            FilmList filmList = filmListRepository.findByListIdAndUser(listId, user)
                    .orElseThrow(() -> {
                        log.warn("❌ Liste güncelleme başarısız - Liste bulunamadı veya yetki yok: ListId: {}, UserId: {}", listId, userId);
                        return new ResourceNotFoundException("Güncellenecek liste bulunamadı veya bu listeyi güncelleme yetkiniz yok: " + listId);
                    });

            log.debug("Güncellenecek liste bulundu - ListId: {}, CurrentName: '{}'", listId, filmList.getName());

            // Güncellenen alanları logla
            StringBuilder changesLog = new StringBuilder();
            
            // DTO'dan gelen null olmayan ve boş olmayan değerlerle güncelleme yap
            if (updateRequestDTO.getName() != null && !updateRequestDTO.getName().isBlank()) {
                String oldName = filmList.getName();
                filmList.setName(updateRequestDTO.getName());
                changesLog.append(String.format("Name: '%s' -> '%s', ", oldName, updateRequestDTO.getName()));
            }
            if (updateRequestDTO.getDescription() != null) { // Açıklama boş string olabilir veya null
                String oldDesc = filmList.getDescription();
                filmList.setDescription(updateRequestDTO.getDescription());
                changesLog.append(String.format("Description: '%s' -> '%s', ", oldDesc, updateRequestDTO.getDescription()));
            }
            if (updateRequestDTO.getTag() != null && !updateRequestDTO.getTag().isBlank()) { // TAG DB'de NOT NULL olduğu için boş olamaz
                String oldTag = filmList.getTag();
                filmList.setTag(updateRequestDTO.getTag());
                changesLog.append(String.format("Tag: '%s' -> '%s', ", oldTag, updateRequestDTO.getTag()));
            }
            if (updateRequestDTO.getVisible() != null) {
                Integer oldVisibility = filmList.getVisible();
                filmList.setVisible(updateRequestDTO.getVisible());
                changesLog.append(String.format("Visibility: %d -> %d, ", oldVisibility, updateRequestDTO.getVisible()));
            }
            
            FilmList updatedList = filmListRepository.save(filmList);
            long duration = System.currentTimeMillis() - startTime;
            
            String changes = changesLog.length() > 0 ? changesLog.substring(0, changesLog.length() - 2) : "No changes";
            log.info("✅ Liste başarıyla güncellendi - UserId: {}, ListId: {}, Changes: [{}], Duration: {}ms", 
                    userId, listId, changes, duration);
            
            return mapToFilmListDetailDTO(updatedList);
            
        } catch (UserNotFoundException | ResourceNotFoundException e) {
            throw e; // Zaten loglandı
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Liste güncelleme hatası - UserId: {}, ListId: {}, Error: {}, Duration: {}ms", 
                    userId, listId, e.getMessage(), duration, e);
            throw new RuntimeException("Liste güncellenirken beklenmeyen bir hata oluştu", e);
        }
    }

    @Override
    public void deleteList(String userId, String listId) {
        log.debug("Liste silme işlemi başlatıldı - UserId: {}, ListId: {}", userId, listId);
        long startTime = System.currentTimeMillis();
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Liste silme başarısız - Kullanıcı bulunamadı: {}", userId);
                        return new UserNotFoundException("Kullanıcı bulunamadı: " + userId);
                    });
                    
            // Kullanıcının sadece kendi listesini silebilmesini sağla
            FilmList filmList = filmListRepository.findByListIdAndUser(listId, user)
                    .orElseThrow(() -> {
                        log.warn("❌ Liste silme başarısız - Liste bulunamadı veya yetki yok: ListId: {}, UserId: {}", listId, userId);
                        return new ResourceNotFoundException("Silinecek liste bulunamadı veya bu listeyi silme yetkiniz yok: " + listId);
                    });

            String listName = filmList.getName();
            int filmCount = filmList.getFilmListInfos().size();
            
            log.debug("Silinecek liste bulundu - ListId: {}, Name: '{}', FilmCount: {}", listId, listName, filmCount);

            // FilmList entity'sindeki @OneToMany ilişkisinde cascade = CascadeType.ALL ve orphanRemoval = true
            // ayarlandığı için, FilmList silindiğinde ilişkili FilmListInfo kayıtları da otomatik silinir.
            filmListRepository.delete(filmList);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Liste başarıyla silindi - UserId: {}, ListId: {}, ListName: '{}', DeletedFilmCount: {}, Duration: {}ms", 
                    userId, listId, listName, filmCount, duration);
                    
        } catch (UserNotFoundException | ResourceNotFoundException e) {
            throw e; // Zaten loglandı
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Liste silme hatası - UserId: {}, ListId: {}, Error: {}, Duration: {}ms", 
                    userId, listId, e.getMessage(), duration, e);
            throw new RuntimeException("Liste silinirken beklenmeyen bir hata oluştu", e);
        }
    }

    @Override
    public FilmListDetailDTO addFilmToList(String userId, String listId, FilmToListRequestDTO filmRequestDTO) {
        String filmId = filmRequestDTO.getFilmId();
        log.debug("Listeye film ekleme işlemi başlatıldı - UserId: {}, ListId: {}, FilmId: {}", userId, listId, filmId);
        long startTime = System.currentTimeMillis();
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Film ekleme başarısız - Kullanıcı bulunamadı: {}", userId);
                        return new UserNotFoundException("Kullanıcı bulunamadı: " + userId);
                    });
                    
            // Kullanıcının sadece kendi listesine film ekleyebilmesini sağla
            FilmList filmList = filmListRepository.findByListIdAndUser(listId, user)
                    .orElseThrow(() -> {
                        log.warn("❌ Film ekleme başarısız - Liste bulunamadı veya yetki yok: ListId: {}, UserId: {}", listId, userId);
                        return new ResourceNotFoundException("Film eklenecek liste bulunamadı veya bu listeye erişim yetkiniz yok: " + listId);
                    });

            if (!filmInfoRepository.existsById(filmId)) {
                log.warn("❌ Film ekleme başarısız - Film bulunamadı: FilmId: {}, ListId: {}", filmId, listId);
                throw new FilmNotFoundException("Listeye eklenecek film bulunamadı: " + filmId);
            }

            FilmListInfoId filmListInfoId = new FilmListInfoId(listId, filmId);
            if (filmListInfoRepository.existsById(filmListInfoId)) {
                log.warn("⚠ Film zaten listede mevcut - FilmId: {}, ListId: {}, ListName: '{}'", 
                        filmId, listId, filmList.getName());
                // Hata fırlatmak yerine mevcut listeyi döndürmek daha kullanıcı dostu olabilir.
                return mapToFilmListDetailDTO(filmList);
            }

            log.debug("Film listeye eklenebilir - FilmId: {}, ListId: {}, ListName: '{}'", filmId, listId, filmList.getName());

            FilmListInfo filmListInfo = FilmListInfo.builder()
                    .id(filmListInfoId)
                    .filmList(filmList) // FilmList ile ilişkiyi kur
                    .user(user) // FilmListInfo'daki USER_ID'yi set et (DB script'ine göre listenin sahibi)
                    .build();
            
            filmListInfoRepository.save(filmListInfo);
            
            // Listenin en güncel halini (içindeki filmlerle birlikte) döndür
            FilmList reloadedList = filmListRepository.findById(listId)
                    .orElseThrow(() -> {
                        log.error("❌ Liste yeniden yüklenirken bulunamadı: {}", listId);
                        return new ResourceNotFoundException("Liste yeniden yüklenirken bulunamadı: " + listId);
                    });
                    
            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Film başarıyla listeye eklendi - UserId: {}, FilmId: {}, ListId: {}, ListName: '{}', NewFilmCount: {}, Duration: {}ms", 
                    userId, filmId, listId, filmList.getName(), reloadedList.getFilmListInfos().size(), duration);
            
            return mapToFilmListDetailDTO(reloadedList);
            
        } catch (UserNotFoundException | ResourceNotFoundException | FilmNotFoundException e) {
            throw e; // Zaten loglandı
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Film listeye ekleme hatası - UserId: {}, ListId: {}, FilmId: {}, Error: {}, Duration: {}ms", 
                    userId, listId, filmId, e.getMessage(), duration, e);
            throw new RuntimeException("Film listeye eklenirken beklenmeyen bir hata oluştu", e);
        }
    }

    @Override
    public void removeFilmFromList(String userId, String listId, String filmId) {
        log.debug("Listeden film çıkarma işlemi başlatıldı - UserId: {}, ListId: {}, FilmId: {}", userId, listId, filmId);
        long startTime = System.currentTimeMillis();
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Film çıkarma başarısız - Kullanıcı bulunamadı: {}", userId);
                        return new UserNotFoundException("Kullanıcı bulunamadı: " + userId);
                    });
                    
            // Kullanıcının sadece kendi listesinden film silebilmesini sağla
            FilmList filmList = filmListRepository.findByListIdAndUser(listId, user)
                    .orElseThrow(() -> {
                        log.warn("❌ Film çıkarma başarısız - Liste bulunamadı veya yetki yok: ListId: {}, UserId: {}", listId, userId);
                        return new ResourceNotFoundException("Film silinecek liste bulunamadı veya bu listeye erişim yetkiniz yok: " + listId);
                    });

            FilmListInfoId filmListInfoId = new FilmListInfoId(listId, filmId);
            if (!filmListInfoRepository.existsById(filmListInfoId)) {
                log.warn("⚠ Film listede bulunamadı, silme işlemi atlandı - FilmId: {}, ListId: {}, ListName: '{}'", 
                        filmId, listId, filmList.getName());
                return; // Film zaten listede yoksa bir şey yapma
            }
            
            log.debug("Film listeden çıkarılabilir - FilmId: {}, ListId: {}, ListName: '{}'", filmId, listId, filmList.getName());
            
            filmListInfoRepository.deleteById(filmListInfoId);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Film başarıyla listeden çıkarıldı - UserId: {}, FilmId: {}, ListId: {}, ListName: '{}', Duration: {}ms", 
                    userId, filmId, listId, filmList.getName(), duration);
                    
        } catch (UserNotFoundException | ResourceNotFoundException e) {
            throw e; // Zaten loglandı
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Film listeden çıkarma hatası - UserId: {}, ListId: {}, FilmId: {}, Error: {}, Duration: {}ms", 
                    userId, listId, filmId, e.getMessage(), duration, e);
            throw new RuntimeException("Film listeden çıkarılırken beklenmeyen bir hata oluştu", e);
        }
    }

    // --- Helper Dönüşüm Metotları (Private) ---

    private FilmListDetailDTO mapToFilmListDetailDTO(FilmList filmList) {
        try {
            log.debug("FilmListDetailDTO dönüşümü başlatıldı - ListId: {}", filmList.getListId());
            
            // FilmList entity'sindeki @OneToMany ilişkisi LAZY fetch olarak ayarlandığı için,
            // filmListInfos koleksiyonuna erişildiğinde (örneğin .getFilmListInfos() ile)
            // transactional bir context içinde otomatik olarak DB'den çekilir.
            List<FilmListInfo> infos = filmList.getFilmListInfos();
            
            List<String> filmIds = infos.stream()
                                        .map(info -> info.getId().getFilmId())
                                        .collect(Collectors.toList());
            
            log.debug("Film ID'leri toplandı - ListId: {}, FilmCount: {}", filmList.getListId(), filmIds.size());
            
            List<FilmSummaryDTO> filmSummaries;
            if (filmIds.isEmpty()) {
                filmSummaries = Collections.emptyList();
                log.debug("Liste boş, film summary'leri boş liste olarak ayarlandı - ListId: {}", filmList.getListId());
            } else {
                // FilmInfo'ları topluca çekmek, her biri için ayrı sorgu atmaktan daha verimlidir.
                Map<String, FilmInfo> filmInfoMap = filmInfoRepository.findAllById(filmIds).stream()
                        .collect(Collectors.toMap(FilmInfo::getId, fi -> fi));
                
                int foundFilms = filmInfoMap.size();
                if (foundFilms != filmIds.size()) {
                    log.warn("⚠ Bazı filmler bulunamadı - ListId: {}, Expected: {}, Found: {}, MissingFilms: {}", 
                            filmList.getListId(), filmIds.size(), foundFilms, 
                            filmIds.stream().filter(id -> !filmInfoMap.containsKey(id)).collect(Collectors.toList()));
                }
                
                filmSummaries = filmIds.stream()
                        .map(filmInfoMap::get) // Map'ten FilmInfo'yu al
                        .filter(Objects::nonNull) // FilmInfo bulunamazsa (veri tutarsızlığı durumu) atla
                        .map(this::convertToFilmSummaryDTO)
                        .collect(Collectors.toList());
                        
                log.debug("Film summary'leri oluşturuldu - ListId: {}, ProcessedFilms: {}", 
                        filmList.getListId(), filmSummaries.size());
            }

            FilmListDetailDTO result = FilmListDetailDTO.builder()
                    .listId(filmList.getListId())
                    .name(filmList.getName())
                    .description(filmList.getDescription())
                    .tag(filmList.getTag())
                    .visibility(filmList.getVisible())
                    .status(filmList.getStatus())
                    .owner(convertToUserDTO(filmList.getUser())) // User entity'sini UserDTO'ya dönüştür
                    .created(filmList.getCreated())
                    .lastUpd(filmList.getLastUpd())
                    .films(filmSummaries)
                    // .isOrdered(filmList.getIsOrdered()) // Eğer FilmList entity'sinde bu alan varsa
                    .build();
                    
            log.debug("FilmListDetailDTO başarıyla oluşturuldu - ListId: {}, FilmCount: {}", 
                    filmList.getListId(), result.getFilms().size());
            
            return result;
            
        } catch (Exception e) {
            log.error("❌ FilmListDetailDTO dönüşüm hatası - ListId: {}, Error: {}", 
                    filmList.getListId(), e.getMessage(), e);
            throw new RuntimeException("Liste detay dönüşümü sırasında hata oluştu", e);
        }
    }

    private FilmListSummaryDTO mapToFilmListSummaryDTO(FilmList filmList) {
        try {
            int filmCount = filmList.getFilmListInfos().size();
            UserSummaryDTO ownerDTO = mapToUserSummary(filmList.getUser());

            // ------------------ Yeni Mantık ------------------
            List<FilmSummaryDTO> filmPreviews = Collections.emptyList();
            if (filmCount > 0) {
                List<String> filmIdsForPreview = filmList.getFilmListInfos().stream()
                        .map(info -> info.getId().getFilmId())
                        .limit(3)
                        .collect(Collectors.toList());

                if (!filmIdsForPreview.isEmpty()) {
                    filmPreviews = filmInfoRepository.findAllById(filmIdsForPreview).stream()
                            .map(this::convertToFilmSummaryDTO)
                            .collect(Collectors.toList());
                }
            }
            // --------------------------------------------------
    
            return FilmListSummaryDTO.builder()
                    .listId(filmList.getListId())
                    .name(filmList.getName())
                    .tag(filmList.getTag())
                    .filmCount(filmCount)
                    .visibility(filmList.getVisible())
                    .owner(ownerDTO) // owner bilgisi eklendi
                    .films(filmPreviews)
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ FilmListSummaryDTO dönüşüm hatası - ListId: {}, Error: {}", 
                    filmList.getListId(), e.getMessage(), e);
            throw new RuntimeException("Liste özet dönüşümü sırasında hata oluştu", e);
        }
    }

    // Bu metotlar UserInteractionServiceImpl'de de vardı.
    // Ortak bir Mapper utility sınıfına taşımak iyi bir pratik olabilir.
    private UserDTO convertToUserDTO(User user) {
        try {
            if (user == null) {
                log.warn("⚠ UserDTO dönüşümü için null User entity");
                return null;
            }
            
            return UserDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .authType(user.getAuthentication() != null ? user.getAuthentication().getId() : null)
                    .providerId(user.getProviderId()) // UserDTO'nuzda bu alan varsa
                    .createdDate(user.getCreatedDate())
                    .lastUpdatedDate(user.getLastUpdatedDate())
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ UserDTO dönüşüm hatası - UserId: {}, Error: {}", 
                    user != null ? user.getId() : "null", e.getMessage(), e);
            throw new RuntimeException("User DTO dönüşümü sırasında hata oluştu", e);
        }
    }

    private FilmSummaryDTO convertToFilmSummaryDTO(FilmInfo filmInfo) {
        try {
            if (filmInfo == null) {
                log.warn("⚠ FilmSummaryDTO dönüşümü için null FilmInfo entity");
                return null;
            }
            
            return FilmSummaryDTO.builder()
                    .id(filmInfo.getId())
                    .title(filmInfo.getName())
                    .imageUrl(generateImageUrl(filmInfo.getId()))
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ FilmSummaryDTO dönüşüm hatası - FilmId: {}, Error: {}", 
                    filmInfo != null ? filmInfo.getId() : "null", e.getMessage(), e);
            throw new RuntimeException("Film özet dönüşümü sırasında hata oluştu", e);
        }
    }
    
    private String getBaseUrl() {
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (Exception e) {
            return "http://localhost:8080";
        }
    }
    
    private String generateImageUrl(String filmId) {
        return getBaseUrl() + "/api/v1/films/image/" + filmId;
    }
    
    private String generateAvatarUrl(String avatarId) {
        if (avatarId == null) return null;
        return getBaseUrl() + "/api/v1/avatars/" + avatarId + "/image";
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilmListSummaryDTO> getLatestPublicLists(int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    List<FilmList> publicLists = filmListRepository.findByVisibleAndStatusOrderByCreatedDesc(VISIBILITY_PUBLIC, STATUS_ACTIVE, pageable);

    return publicLists.stream()
            .map(this::mapToFilmListSummaryDTO)
            .collect(Collectors.toList());
    }

    private UserSummaryDTO mapToUserSummary(User user) {
        if (user == null) return null;
        return UserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .avatarImageUrl(generateAvatarUrl(user.getAvatarId()))
                .build();
    }
}