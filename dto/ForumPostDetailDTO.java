package com.example.moodmovies.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ForumPostDetailDTO {
    private String id;
    private String title;
    private String tag;
    private String context;
    private UserSummaryDTO author;
    private LocalDateTime created;
    private LocalDateTime lastUpd;
    private List<ForumCommentDTO> comments;
}