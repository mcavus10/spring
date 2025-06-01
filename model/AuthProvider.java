package com.example.moodmovies.model;

/**
 * Uygulamada desteklenen kimlik doğrulama sağlayıcılarını temsil eder.
 * Bu sınıf, veritabanındaki MOODMOVIES_AUTHENTICATION tablosundan AUTH_ID değerlerine karşılık gelir.
 */
public class AuthProvider {
    // Kimlik doğrulama tipleri için sabit değerler
    public static final String LOCAL = "LOCAL";
    public static final String GOOGLE = "GOOGLE";
    public static final String FACEBOOK = "FACEBOOK";
    
    // Instance oluşturulmasını engellemek için private constructor
    private AuthProvider() {
        // Utility class, instance oluşturulmamalı
    }
}
