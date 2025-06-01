package com.example.moodmovies.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

/**
 * JWT token'laru0131nu0131 gu00fcvenli HTTP-only cookie olarak yu00f6netmek iu00e7in servis su0131nu0131fu0131.
 */
@Service
public class CookieService {

    @Value("${app.cookie.domain:localhost}")
    private String domain;

    @Value("${app.cookie.path:/}")
    private String path;

    @Value("${app.cookie.secure:false}")
    private boolean secure;

    @Value("${app.cookie.http-only:true}")
    private boolean httpOnly;

    @Value("${app.cookie.max-age:86400}")
    private int maxAge;
    
    // Standardize edilmiş cookie isimleri - OAuth2 akışıyla uyumlu
    private static final String ACCESS_TOKEN_COOKIE_NAME = "jwt";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    /**
     * JWT access token iu00e7in HTTP-only cookie oluu015fturur.
     * 
     * @param token JWT token
     * @return HTTP cookie nesnesi
     */
    public HttpCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, token)
                .domain(domain)
                .path(path)
                .maxAge(maxAge)
                .httpOnly(httpOnly)
                .secure(secure)
                .build();
    }

    /**
     * JWT refresh token iu00e7in HTTP-only cookie oluu015fturur.
     * 
     * @param token JWT refresh token
     * @return HTTP cookie nesnesi
     */
    public HttpCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token)
                .domain(domain)
                .path(path)
                .maxAge(7 * maxAge) // Refresh token'laru0131 daha uzun su00fcreli (1 hafta) yap
                .httpOnly(httpOnly)
                .secure(secure)
                .build();
    }

    /**
     * Access token cookie'sini siler.
     * 
     * @return Su0131fu0131rlanmu0131u015f HTTP cookie nesnesi
     */
    public HttpCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .domain(domain)
                .path(path)
                .maxAge(0) // u00d6mru00fc su0131fu0131r olarak ayarlandu0131u011fu0131nda cookie silinir
                .httpOnly(httpOnly)
                .secure(secure)
                .build();
    }

    /**
     * Refresh token cookie'sini siler.
     * 
     * @return Su0131fu0131rlanmu0131u015f HTTP cookie nesnesi
     */
    public HttpCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .domain(domain)
                .path(path)
                .maxAge(0) // u00d6mru00fc su0131fu0131r olarak ayarlandu0131u011fu0131nda cookie silinir
                .httpOnly(httpOnly)
                .secure(secure)
                .build();
    }
    
    /**
     * Access token cookie adu0131nu0131 du00f6ndu00fcru00fcr
     */
    public String getAccessTokenCookieName() {
        return ACCESS_TOKEN_COOKIE_NAME;
    }
    
    /**
     * Refresh token cookie adu0131nu0131 du00f6ndu00fcru00fcr
     */
    public String getRefreshTokenCookieName() {
        return REFRESH_TOKEN_COOKIE_NAME;
    }
}
