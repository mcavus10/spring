package com.example.moodmovies.repository;

import com.example.moodmovies.model.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, String> {
    // Ek sorgulara ihtiyaç olursa buraya eklenebilir
}