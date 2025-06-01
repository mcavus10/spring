package com.example.moodmovies.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        
        log.error("Yetkilendirme hatası: {}", authException.getMessage());
        
        // 401 Unauthorized hatası dön
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                "Erişim reddedildi. Lütfen giriş yapınız.");
    }
}
