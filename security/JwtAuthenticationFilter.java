package com.example.moodmovies.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // JWT token'ı header veya cookie'den çıkar
            String jwt = getJwtFromRequest(request);
            
            // CORS için özel header'lar ekle
            if (request.getHeader("Origin") != null) {
                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
                response.setHeader("Access-Control-Max-Age", "3600");
            }

            // Token geçerli ise kullanıcıyı doğrula
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String userId = tokenProvider.getUserIdFromJWT(jwt);
                log.debug("JWT token doğrulandı, kullanıcı ID: {}", userId);

                UserDetails userDetails = userDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (StringUtils.hasText(jwt)) {
                log.warn("JWT token bulunamadı veya doğrulanamadı");
            }
        } catch (Exception ex) {
            log.error("Kullanıcı kimlik doğrulaması yapılamadı: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Önce cookie'den, yoksa Authorization header'dan JWT token'ı çıkarır
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. Önce Cookie'den JWT'yi al
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (StringUtils.hasText(token)) {
                        log.debug("JWT token cookie'den başarıyla alındı");
                        return token;
                    }
                }
            }
        }
        
        // 2. Cookie'de yoksa Header'dan dene
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.debug("JWT token Authorization header'dan alındı");
            return bearerToken.substring(7);
        }
        
        log.trace("JWT token bulunamadı (ne cookie, ne header)");
        return null;
    }
}
