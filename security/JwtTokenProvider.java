package com.example.moodmovies.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private Long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshTokenExpirationMs;

    /**
     * Kullanıcıdan gelen Authentication nesnesine dayanarak JWT token oluşturur
     * @param authentication Spring Security Authentication nesnesi
     * @return oluşturulan JWT token string'i
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * User ID'sinden yeni bir JWT token oluşturur
     * @param userId Kullanıcı ID'si
     * @return oluşturulan JWT token string'i
     */
    public String generateTokenFromUserId(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * Refresh token oluşturur
     * @param userId Kullanıcı ID'si
     * @return oluşturulan refresh token
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);
        
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * JWT token'dan kullanıcı ID'sini çıkarır
     * @param token JWT token string
     * @return token'dan çıkarılan user ID'si
     */
    public String getUserIdFromJWT(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Token'ın geçerliliğini kontrol eder
     * @param authToken kontrol edilecek JWT token
     * @return token geçerli ise true, değilse false
     */
    public boolean validateToken(String authToken) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Geçersiz JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Süresi dolmuş JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Desteklenmeyen JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string boş");
        }
        return false;
    }

    public Long getJwtExpirationMs() {
        return jwtExpirationMs;
    }
}
