package com.example.moodmovies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDataDTO {
    private UserDTO user;
    private List<FilmSummaryDTO> favorites;
    private List<FilmListSummaryDTO> lists;
    private List<RatedFilmDTO> ratings;
}