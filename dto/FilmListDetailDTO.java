package com.example.moodmovies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder // Bu anotasyonun olduğundan emin ol
@NoArgsConstructor
@AllArgsConstructor
public class FilmListDetailDTO {
    private String listId;
    private String name;
    private String description;
    private String tag;
    private Integer visibility; // Bu alanın var olduğundan ve tipinin Integer olduğundan emin ol
    private Integer status;
    private UserDTO owner;
    private LocalDateTime created;
    private LocalDateTime lastUpd;
    private List<FilmSummaryDTO> films;
    // private boolean isOrdered; // DB'de saklanmıyorsa bu DTO'da da olmayabilir
}