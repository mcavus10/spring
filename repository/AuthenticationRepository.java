package com.example.moodmovies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.moodmovies.model.Authentication;

/**
 * Kimlik dou011frulama sau011flayu0131cu0131laru0131 iu00e7in repository.
 * MOODMOVIES_AUTHENTICATION tablosu iu00e7in CRUD iu015flemleri sunu0131fu0131.
 */
@Repository
public interface AuthenticationRepository extends JpaRepository<Authentication, String> {
    // Temel CRUD iu015flemleri JpaRepository'den gelmektedir
    // Bu interface'e u00f6zel sorgular burada tanu0131mlanabilir
}
