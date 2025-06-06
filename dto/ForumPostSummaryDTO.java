package com.example.moodmovies.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ForumPostSummaryDTO {
    private String id;
    private String title;
    private String tag;
    private UserSummaryDTO author;
    private long commentCount;
    private LocalDateTime created;
}