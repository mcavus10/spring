package com.example.moodmovies.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kimlik dou011frulama yu00f6ntemlerini temsil eden entity su0131nu0131fu0131.
 * MOODMOVIES_AUTHENTICATION tablosundaki deu011ferleri maplemek iu00e7in kullanu0131lu0131r.
 */
@Entity
@Table(name = "MOODMOVIES_AUTHENTICATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authentication {

    @Id
    @Column(name = "AUTH_ID", length = 15, nullable = false)
    private String id;
    
    @Column(name = "AUTH_NAME", length = 20, nullable = true)
    private String name;
    
    @OneToMany(mappedBy = "authentication")
    private List<User> users;
    
    // Sabit deu011ferler - veritabanu0131ndaki AUTH_ID deu011ferleri ile eu015fleu015fmelidir
    public static final String LOCAL = "LOCAL";
    public static final String GOOGLE = "GOOGLE";
    public static final String FACEBOOK = "FACEBOOK";
}
