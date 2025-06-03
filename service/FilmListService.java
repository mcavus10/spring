package com.example.moodmovies.service;

import com.example.moodmovies.dto.ListCreateRequestDTO; // Düzeltilmiş DTO adı
import com.example.moodmovies.dto.FilmListDetailDTO;
import com.example.moodmovies.dto.FilmListSummaryDTO;
import com.example.moodmovies.dto.ListUpdateRequestDTO; // Düzeltilmiş DTO adı
import com.example.moodmovies.dto.FilmToListRequestDTO;

import java.util.List;

public interface FilmListService {

    FilmListDetailDTO createList(String userId, ListCreateRequestDTO createRequestDTO);

    List<FilmListSummaryDTO> getUserLists(String userId, boolean includePrivate);

    FilmListDetailDTO getListDetails(String listId, String currentUserId);

    FilmListDetailDTO updateList(String userId, String listId, ListUpdateRequestDTO updateRequestDTO);

    void deleteList(String userId, String listId);

    FilmListDetailDTO addFilmToList(String userId, String listId, FilmToListRequestDTO filmRequestDTO);

    void removeFilmFromList(String userId, String listId, String filmId);
}