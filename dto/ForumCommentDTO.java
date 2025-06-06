package com.example.moodmovies.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ForumCommentDTO {
    private String id;
    private String comment;
    private UserSummaryDTO author;
    private LocalDateTime created;
    private LocalDateTime lastUpd;
}