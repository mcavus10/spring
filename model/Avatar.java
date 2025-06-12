package com.example.moodmovies.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MOODMOVIES_AVATARS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Avatar {
    @Id
    @Column(name = "AVATAR_ID", length = 15, nullable = false)
    private String avatarId;

    @Lob
    @Column(name = "ImageByte")
    private byte[] imageByte;
} 