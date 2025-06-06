package com.example.moodmovies.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MOODMOVIES_FORUM")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumPost {

    @Id
    @GeneratedValue(generator = "forum_post_id_generator")
    @GenericGenerator(name = "forum_post_id_generator", strategy = "com.example.moodmovies.config.ForumPostIdGenerator")
    @Column(name = "CONTEXT_ID")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "TITLE", nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "CONTEXT", nullable = false)
    private String context;

    @Column(name = "TAG", length = 50)
    private String tag;

    @CreationTimestamp
    @Column(name = "CREATED", nullable = false, updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "LAST_UPD")
    private LocalDateTime lastUpd;

    @OneToMany(mappedBy = "forumPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("created ASC") // Yorumları eskiden yeniye sırala
    private List<ForumComment> comments = new ArrayList<>();
}