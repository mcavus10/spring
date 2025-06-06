package com.example.moodmovies.service;

import com.example.moodmovies.dto.FilmListDetailDTO;
import com.example.moodmovies.dto.FilmListSummaryDTO;
import com.example.moodmovies.dto.FilmToListRequestDTO;
import com.example.moodmovies.dto.ListCreateRequestDTO;
import com.example.moodmovies.dto.ListUpdateRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface FilmListService {

    FilmListDetailDTO createList(String userId, ListCreateRequestDTO createRequestDTO);

    List<FilmListSummaryDTO> getUserLists(String userId, boolean includePrivate);

    FilmListDetailDTO getListDetails(String listId, String currentUserId);

    FilmListDetailDTO updateList(String userId, String listId, ListUpdateRequestDTO updateRequestDTO);

    void deleteList(String userId, String listId);

    FilmListDetailDTO addFilmToList(String userId, String listId, FilmToListRequestDTO filmRequestDTO);

    void removeFilmFromList(String userId, String listId, String filmId);

    // YENİ EKLENEN METOT
    List<FilmListSummaryDTO> getLatestPublicLists(int limit);
}