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
public class FilmListSummaryDTO {
    private String listId;
    private String name;
    private String tag;
    private int filmCount;
    private Integer visibility;
    private UserSummaryDTO owner; // Hatanın çözümü için bu satır eklendi
    
    // Listedeki ilk birkaç filmin özetini tutacak
    private List<FilmSummaryDTO> films;
}