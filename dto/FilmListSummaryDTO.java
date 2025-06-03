package com.example.moodmovies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder // Bu anotasyonun olduğundan emin ol
@NoArgsConstructor
@AllArgsConstructor
public class FilmListSummaryDTO {
    private String listId;
    private String name;
    private String tag;
    private int filmCount;
    private Integer visibility; // Bu alanın var olduğundan ve tipinin Integer olduğundan emin ol
}