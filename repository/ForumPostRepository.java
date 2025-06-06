package com.example.moodmovies.repository;

import com.example.moodmovies.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, String> {

    // N+1 problemini çözmek için post ID listesine göre yorum sayılarını tek sorguda getiren metot.
    @Query("SELECT c.forumPost.id, COUNT(c.id) FROM ForumComment c WHERE c.forumPost.id IN :postIds GROUP BY c.forumPost.id")
    List<Object[]> countCommentsByPostIds(@Param("postIds") List<String> postIds);
}