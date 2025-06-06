package com.example.moodmovies.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "MOODMOVIES_COMMENT")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumComment {

    @Id
    @GeneratedValue(generator = "forum_comment_id_generator")
    @GenericGenerator(name = "forum_comment_id_generator", strategy = "com.example.moodmovies.config.ForumCommentIdGenerator")
    @Column(name = "COMMENT_ID")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONTEXT_ID", nullable = false)
    private ForumPost forumPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Lob
    @Column(name = "COMMENT", nullable = false)
    private String comment;

    @CreationTimestamp
    @Column(name = "CREATED", nullable = false, updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "LAST_UPD")
    private LocalDateTime lastUpd;
}