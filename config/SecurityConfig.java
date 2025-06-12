package com.example.moodmovies.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import com.example.moodmovies.security.JwtAuthenticationEntryPoint;
import com.example.moodmovies.security.JwtAuthenticationFilter;
import com.example.moodmovies.security.OAuth2AuthenticationFailureHandler;
import com.example.moodmovies.security.OAuth2AuthenticationSuccessHandler;
import com.example.moodmovies.security.OAuth2UserServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2UserServiceImpl oauth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // REST API için CSRF devre dışı
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS ayarlarını etkinleştir
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT için oturum yok
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                // Endpoint bazlı erişim kuralları
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/test").permitAll() // Test endpoint'ini public yap
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/refresh-token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/forum/**").permitAll()
                        .requestMatchers("/api/v1/forum/**").authenticated()
                        .requestMatchers("/api/v1/auth/**").permitAll() // Kimlik doğrulama endpoint'leri herkese açık
                        .requestMatchers("/h2-console/**").permitAll() // H2 konsolu geliştirme amaçlı açık
                        .requestMatchers("/error").permitAll() // Hata sayfası açık
                        .requestMatchers("/api/v1/interactions/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/films/**").permitAll() // Film listesi, detay ve resim GET isteklerine izin ver
                        .requestMatchers(HttpMethod.GET, "/api/v1/lists/public/**").permitAll() // Herkese açık listeler
                        .requestMatchers(HttpMethod.GET, "/api/v1/lists/*/").permitAll() // Liste detayları (public kontrol service'de yapılır)
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/popular/**").permitAll() // Popüler kullanıcılar
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/*").permitAll() // Kullanıcı profilleri
                        .requestMatchers("/api/v1/lists/**").authenticated() // Diğer liste işlemleri
                        .requestMatchers("/api/v1/users/**").authenticated() // Diğer kullanıcı işlemleri
                        .anyRequest().authenticated() // Diğer tüm istekler doğrulama gerektirir
                )
                // OAuth2 yapılandırması
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                // JWT filtresini UsernamePasswordAuthenticationFilter'dan önce ekle
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // H2 konsolu için gerekli
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000","http://192.168.68.102","exp://192.168.68.102:8081", "exp://192.168.68.102:8082", "exp://192.168.68.102:19000",  // Expo Go için
                "exp://localhost:19000"    ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        
        // Daha kapsamlı header izinleri
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "Accept", 
            "X-Requested-With", 
            "Cache-Control", 
            "Access-Control-Allow-Headers", 
            "Access-Control-Allow-Origin", 
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"
        ));
        
        // Çerezler için önemli - istemcinin kimlik bilgilerini göndermesine izin ver
        configuration.setAllowCredentials(true);
        
        // CORS ön-uçuş istekleri için maksimum yaş
        configuration.setMaxAge(3600L);
        
        // İstemciye hangi başlıkları gösterebileceğimizi belirt
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Disposition", 
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Credentials", 
            "Set-Cookie"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
