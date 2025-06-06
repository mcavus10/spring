package com.example.moodmovies.repository;

import com.example.moodmovies.model.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, String> {
    // Temel CRUD işlemleri yeterli olacaktır.
    // İhtiyaç olursa özel sorgular eklenebilir.
}