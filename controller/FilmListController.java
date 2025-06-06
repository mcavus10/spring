package com.example.moodmovies.controller;

import com.example.moodmovies.dto.ListCreateRequestDTO;
import com.example.moodmovies.dto.FilmListDetailDTO;
import com.example.moodmovies.dto.FilmListSummaryDTO;
import com.example.moodmovies.dto.ListUpdateRequestDTO;
import com.example.moodmovies.dto.FilmToListRequestDTO;
import com.example.moodmovies.security.UserPrincipal; // Projendeki UserPrincipal yolu
import com.example.moodmovies.service.FilmListService; // Projendeki FilmListService yolu
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lists") // Liste işlemleri için ana URL yolu
@RequiredArgsConstructor
@Slf4j
public class FilmListController {

    private final FilmListService filmListService;

    /**
     * Giriş yapmış kullanıcının yeni bir film listesi oluşturmasını sağlar.
     * İstek gövdesinde (request body) Liste adı, etiket, görünürlük ve açıklama bilgileri beklenir.
     * Başarılı olursa HTTP 201 (Created) ve oluşturulan listenin detayları döner.
     */
    @PostMapping
    public ResponseEntity<FilmListDetailDTO> createList(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ListCreateRequestDTO createRequestDTO) {
        
        long startTime = System.currentTimeMillis();
        String userId = currentUser != null ? currentUser.getId() : "anonymous";
        
        log.info("🎬 Liste oluşturma isteği - UserId: {}, ListName: '{}', Tag: '{}', Visibility: {}", 
                userId, createRequestDTO.getName(), createRequestDTO.getTag(), createRequestDTO.getVisible());
        
        try {
            // currentUser null kontrolü Spring Security tarafından zaten yapılır,
            // eğer endpoint @Authenticated ise ve token yoksa 401 döner.
            // Ancak yine de bir savunma katmanı olarak eklenebilir veya UserPrincipal'ın null olamayacağı varsayılabilir.
            if (currentUser == null) {
                log.warn("❌ Yetkisiz liste oluşturma girişimi - Token eksik");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            FilmListDetailDTO createdList = filmListService.createList(currentUser.getId(), createRequestDTO);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Liste başarıyla oluşturuldu - UserId: {}, ListId: {}, Duration: {}ms", 
                    userId, createdList.getListId(), duration);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdList);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Liste oluşturma endpoint hatası - UserId: {}, ListName: '{}', Error: {}, Duration: {}ms", 
                    userId, createRequestDTO.getName(), e.getMessage(), duration, e);
            throw e; // Global exception handler'a bırak
        }
    }

    /**
     * Giriş yapmış kullanıcının kendi tüm aktif listelerini özet olarak getirir (özel olanlar dahil).
     */
    @GetMapping
    public ResponseEntity<List<FilmListSummaryDTO>> getCurrentUserLists(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        long startTime = System.currentTimeMillis();
        String userId = currentUser != null ? currentUser.getId() : "anonymous";
        
        log.info("📋 Kullanıcının kendi listeleri istendi - UserId: {}", userId);
        
        try {
            if (currentUser == null) {
                log.warn("❌ Yetkisiz liste görüntüleme girişimi - Token eksik");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // `includePrivate = true` kendi listelerini istediği için özel olanları da getirir.
            List<FilmListSummaryDTO> lists = filmListService.getUserLists(currentUser.getId(), true);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Kullanıcının listeleri getirildi - UserId: {}, Count: {}, Duration: {}ms", 
                    userId, lists.size(), duration);
            
            return ResponseEntity.ok(lists);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Kullanıcı listeleri endpoint hatası - UserId: {}, Error: {}, Duration: {}ms", 
                    userId, e.getMessage(), duration, e);
            throw e; // Global exception handler'a bırak
        }
    }
    
    /**
     * Belirli bir kullanıcının (userId ile belirtilen) listelerini getirir.
     * Eğer isteği yapan kişi, profiline bakılan kişiyle aynıysa özel listeler de gelir.
     * Aksi halde sadece herkese açık (public) ve aktif listeler gelir.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FilmListSummaryDTO>> getUserPublicOrAllLists(
            @PathVariable String userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        long startTime = System.currentTimeMillis();
        String requesterId = currentUser != null ? currentUser.getId() : "anonymous";
        boolean includePrivate = currentUser != null && currentUser.getId().equals(userId);
        
        log.info("👤 Kullanıcı listeleri istendi - TargetUserId: {}, RequesterId: {}, IncludePrivate: {}", 
                userId, requesterId, includePrivate);
        
        try {
            List<FilmListSummaryDTO> lists = filmListService.getUserLists(userId, includePrivate);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Kullanıcı listeleri getirildi - TargetUserId: {}, RequesterId: {}, Count: {}, Duration: {}ms", 
                    userId, requesterId, lists.size(), duration);
            
            return ResponseEntity.ok(lists);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Kullanıcı listeleri endpoint hatası - TargetUserId: {}, RequesterId: {}, Error: {}, Duration: {}ms", 
                    userId, requesterId, e.getMessage(), duration, e);
            throw e; // Global exception handler'a bırak
        }
    }

    /**
     * Belirli bir listenin (listId ile belirtilen) detaylarını getirir.
     * Eğer liste özelse (private), sadece listenin sahibi bu detayı görebilir.
     * Herkese açık (public) listeleri herkes görebilir.
     * Anonim kullanıcılar (giriş yapmamış) sadece public listeleri görebilir.
     */
    @GetMapping("/{listId}")
    public ResponseEntity<FilmListDetailDTO> getListDetails(
            @PathVariable String listId,
            @AuthenticationPrincipal UserPrincipal currentUser) { 
        
        long startTime = System.currentTimeMillis();
        String userId = currentUser != null ? currentUser.getId() : "anonymous";
        
        log.info("🔍 Liste detayları istendi - ListId: {}, UserId: {}", listId, userId);
        
        try {
            String currentUserId = (currentUser != null) ? currentUser.getId() : null;
            FilmListDetailDTO listDetails = filmListService.getListDetails(listId, currentUserId);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Liste detayları getirildi - ListId: {}, UserId: {}, FilmCount: {}, Duration: {}ms", 
                    listId, userId, listDetails.getFilms().size(), duration);
            
            return ResponseEntity.ok(listDetails);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Liste detayları endpoint hatası - ListId: {}, UserId: {}, Error: {}, Duration: {}ms", 
                    listId, userId, e.getMessage(), duration, e);
            throw e; // Global exception handler'a bırak
        }
    }

    /**
     * Giriş yapmış kullanıcının kendi listesinin bilgilerini (ad, etiket, görünürlük, açıklama) güncellemesini sağlar.
     */
    @PutMapping("/{listId}")
    public ResponseEntity<FilmListDetailDTO> updateList(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String listId,
            @Valid @RequestBody ListUpdateRequestDTO updateRequestDTO) {
        
        long startTime = System.currentTimeMillis();
        String userId = currentUser != null ? currentUser.getId() : "anonymous";
        
        log.info("✏️ Liste güncelleme isteği - UserId: {}, ListId: {}, UpdateFields: [Name: {}, Tag: {}, Visibility: {}]", 
                userId, listId, 
                updateRequestDTO.getName() != null ? "✓" : "✗",
                updateRequestDTO.getTag() != null ? "✓" : "✗",
                updateRequestDTO.getVisible() != null ? "✓" : "✗");
        
        try {
            if (currentUser == null) {
                log.warn("❌ Yetkisiz liste güncelleme girişimi - Token eksik, ListId: {}", listId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            FilmListDetailDTO updatedList = filmListService.updateList(currentUser.getId(), listId, updateRequestDTO);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Liste başarıyla güncellendi - UserId: {}, ListId: {}, Duration: {}ms", 
                    userId, listId, duration);
            
            return ResponseEntity.ok(updatedList);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Liste güncelleme endpoint hatası - UserId: {}, ListId: {}, Error: {}, Duration: {}ms", 
                    userId, listId, e.getMessage(), duration, e);
            throw e; // Global exception handler'a bırak
        }
    }

    /**
     * Giriş yapmış kullanıcının kendi listesini silmesini sağlar.
     * Başarılı olursa HTTP 204 (No Content) döner.
     */
    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String listId) {
        
        long startTime = System.currentTimeMillis();
        String userId = currentUser != null ? currentUser.getId() : "anonymous";
        
        log.info("🗑️ Liste silme isteği - UserId: {}, ListId: {}", userId, listId);
        
        try {
            if (currentUser == null) {
                log.warn("❌ Yetkisiz liste silme girişimi - Token eksik, ListId: {}", listId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            filmListService.deleteList(currentUser.getId(), listId);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Liste başarıyla silindi - UserId: {}, ListId: {}, Duration: {}ms", 
                    userId, listId, duration);
            
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Liste silme endpoint hatası - UserId: {}, ListId: {}, Error: {}, Duration: {}ms", 
                    userId, listId, e.getMessage(), duration, e);
            throw e; // Global exception handler'a bırak
        }
    }

    /**
     * Giriş yapmış kullanıcının kendi listesine bir film eklemesini sağlar.
     * İstek gövdesinde eklenecek filmin ID'si beklenir.
     */
    @PostMapping("/{listId}/films")
    public ResponseEntity<FilmListDetailDTO> addFilmToList(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String listId,
            @Valid @RequestBody FilmToListRequestDTO filmRequestDTO) {
        
        long startTime = System.currentTimeMillis();
        String userId = currentUser != null ? currentUser.getId() : "anonymous";
        String filmId = filmRequestDTO.getFilmId();
        
        log.info("➕ Listeye film ekleme isteği - UserId: {}, ListId: {}, FilmId: {}", 
                userId, listId, filmId);
        
        try {
            if (currentUser == null) {
                log.warn("❌ Yetkisiz film ekleme girişimi - Token eksik, ListId: {}, FilmId: {}", listId, filmId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            FilmListDetailDTO updatedList = filmListService.addFilmToList(currentUser.getId(), listId, filmRequestDTO);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Film başarıyla listeye eklendi - UserId: {}, ListId: {}, FilmId: {}, NewFilmCount: {}, Duration: {}ms", 
                    userId, listId, filmId, updatedList.getFilms().size(), duration);
            
            return ResponseEntity.ok(updatedList);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Film ekleme endpoint hatası - UserId: {}, ListId: {}, FilmId: {}, Error: {}, Duration: {}ms", 
                    userId, listId, filmId, e.getMessage(), duration, e);
            throw e; // Global exception handler'a bırak
        }
    }

    /**
     * Giriş yapmış kullanıcının kendi listesinden bir filmi çıkarmasını sağlar.
     * Başarılı olursa HTTP 204 (No Content) döner.
     */
    @DeleteMapping("/{listId}/films/{filmId}")
    public ResponseEntity<Void> removeFilmFromList(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String listId,
            @PathVariable String filmId) {
        
        long startTime = System.currentTimeMillis();
        String userId = currentUser != null ? currentUser.getId() : "anonymous";
        
        log.info("➖ Listeden film çıkarma isteği - UserId: {}, ListId: {}, FilmId: {}", 
                userId, listId, filmId);
        
        try {
            if (currentUser == null) {
                log.warn("❌ Yetkisiz film çıkarma girişimi - Token eksik, ListId: {}, FilmId: {}", listId, filmId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            filmListService.removeFilmFromList(currentUser.getId(), listId, filmId);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Film başarıyla listeden çıkarıldı - UserId: {}, ListId: {}, FilmId: {}, Duration: {}ms", 
                    userId, listId, filmId, duration);
            
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Film çıkarma endpoint hatası - UserId: {}, ListId: {}, FilmId: {}, Error: {}, Duration: {}ms", 
                    userId, listId, filmId, e.getMessage(), duration, e);
            throw e; // Global exception handler'a bırak
        }
    }

    /**
     * Herkese açık ve aktif olan listeleri, oluşturulma tarihine göre en yeniden eskiye doğru getirir.
     * Anonim kullanıcılar da erişebilir.
     */
    @GetMapping("/public/latest")
    public ResponseEntity<List<FilmListSummaryDTO>> getLatestPublicLists(
            @RequestParam(defaultValue = "2") int limit) {
        
        long startTime = System.currentTimeMillis();
        
        log.info("🌍 Herkese açık son listeler istendi - Limit: {}", limit);
        
        try {
            List<FilmListSummaryDTO> publicLists = filmListService.getLatestPublicLists(limit);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ Herkese açık listeler getirildi - Count: {}, Duration: {}ms", 
                    publicLists.size(), duration);
            
            return ResponseEntity.ok(publicLists);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Herkese açık listeler endpoint hatası - Error: {}, Duration: {}ms", 
                    e.getMessage(), duration, e);
            throw e; // Global exception handler'a bırak
        }
    }
}