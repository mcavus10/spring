package com.example.moodmovies.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name ="MOODMOVIES_USERS" , uniqueConstraints = {
        @UniqueConstraint(columnNames = "MAIL_ADDRESS")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(generator = "user_id_generator")
    @GenericGenerator(name = "user_id_generator", strategy = "com.example.moodmovies.config.UserIdGenerator")
    @Column(name = "USER_ID", length = 15)
    private String id;

    @NotBlank(message = "username cannot be empty")
    @Size(min = 3, max = 20, message = "username must be between 3 and 20 characters")
    @Column(name = "USER_NAME", length = 80, nullable = true)
    private String name;

    // Şifre hash'leri genellikle 60+ karakter olabilir (BCrypt için)
    @NotBlank(message = "password cannot be empty for local registration")
    @Column(name="USER_PASS", length = 80, nullable = true)
    private String password;

    @NotBlank(message = "email cannot be empty")
    @Email(message = "Please enter a valid email address")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Column(name = "MAIL_ADDRESS", length = 100, nullable = true)
    private String email;
    
    // Authentication tablosu ile iliu015fki
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AUTH_TYPE", referencedColumnName = "AUTH_ID", nullable = false)
    private Authentication authentication;

    @Column(name="PROVIDER_ID", length = 25, nullable = true)
    private String providerId;

    @CreationTimestamp
    @Column(name = "CREATED", nullable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name="LAST_UPD", nullable = true)
    private LocalDateTime lastUpdatedDate;
}
