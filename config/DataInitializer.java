package com.example.moodmovies.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.moodmovies.model.Authentication;
import com.example.moodmovies.model.AuthProvider;
import com.example.moodmovies.repository.AuthenticationRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Uygulama bau015flatu0131ldu0131u011fu0131nda temel verileri yu00fckler.
 * u00d6zellikle H2 bellek iu00e7i veritabanu0131 kullanilu0131rken,
 * her sunucu bau015flatu0131ldu0131u011fu0131nda bu verilerin oluu015fturulmasu0131 gerekir.
 */
@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(AuthenticationRepository authenticationRepository) {
        return args -> {
            log.info("Veritabanu0131 bau015flatu0131lu0131yor - Temel kimlik dou011frulama sau011flayu0131cu0131laru0131 ekleniyor...");
            
            // LOCAL kimlik dou011frulama sau011flayu0131cu0131su0131
            if (!authenticationRepository.existsById(AuthProvider.LOCAL)) {
                Authentication local = new Authentication();
                local.setId(AuthProvider.LOCAL);
                local.setName("Yerel Hesap");
                authenticationRepository.save(local);
                log.info("LOCAL kimlik dou011frulama sau011flayu0131cu0131su0131 eklendi");
            }
            
            // GOOGLE kimlik dou011frulama sau011flayu0131cu0131su0131
            if (!authenticationRepository.existsById(AuthProvider.GOOGLE)) {
                Authentication google = new Authentication();
                google.setId(AuthProvider.GOOGLE);
                google.setName("Google");
                authenticationRepository.save(google);
                log.info("GOOGLE kimlik dou011frulama sau011flayu0131cu0131su0131 eklendi");
            }
            
            // FACEBOOK kimlik dou011frulama sau011flayu0131cu0131su0131
            if (!authenticationRepository.existsById(AuthProvider.FACEBOOK)) {
                Authentication facebook = new Authentication();
                facebook.setId(AuthProvider.FACEBOOK);
                facebook.setName("Facebook");
                authenticationRepository.save(facebook);
                log.info("FACEBOOK kimlik dou011frulama sau011flayu0131cu0131su0131 eklendi");
            }
        };
    }
}
