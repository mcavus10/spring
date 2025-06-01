package com.example.moodmovies.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    
    @Value("${app.oauth2.authorized-redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;
    
    @Value("${app.cookie.domain:localhost}")
    private String cookieDomain;
    
    @Value("${app.cookie.path:/}")
    private String cookiePath;
    
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;
    
    @Value("${app.cookie.http-only:true}")
    private boolean cookieHttpOnly;
    
    @Value("${app.cookie.max-age:86400}")
    private int cookieMaxAge;
    
    protected String determineTargetUrl(Authentication authentication) {
        return redirectUri;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        log.info("OAuth2 authentication successful, redirecting to: {}", redirectUri);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // JWT token üret
        String token = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getId());
        
        // JWT Token için daha güvenli cookie oluşturma
        addSecureCookie(response, "jwt", token, cookieMaxAge);
        
        // Refresh Token için daha güvenli cookie oluşturma
        addSecureCookie(response, "refresh_token", refreshToken, cookieMaxAge * 7);
        
        // Oturum durumunu frontend'e bildirmek için auth_status cookie'si ekleyelim
        addSecureCookie(response, "auth_status", "authenticated", cookieMaxAge);
        
        String targetUrl = determineTargetUrl(authentication);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    /**
     * Daha güvenli cookie oluşturma yöntemi
     */
    private void addSecureCookie(HttpServletResponse response, String name, String value, int maxAge) {
        // Temel cookie nesnesini oluştur
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true); // JavaScript erişimini engelle (güvenlik için)
        
        // Development ortamında güvenli cookie'leri kullanabiliriz
        // Geliştirme sırasında bu yorumu açın: cookie.setSecure(false);
        // Prod ortamında bu ayar olmalı: cookie.setSecure(true);
        cookie.setSecure(false); // Geliştirme ortamı için false, prod için true olmalı
        
        // Önce standard API ile cookie ekle
        response.addCookie(cookie);
        
        // SameSite=None sadece güvenli (HTTPS) bağlantılarda çalışır
        // Geliştirme ortamında Lax kullanıyoruz
        String cookieString = String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax", 
                          name, value, maxAge);
        
        // Aşağıdaki satır prod ortamı için açılmalı:
        // String cookieString = String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None", 
        //                    name, value, maxAge);
        
        // RFC standartlarına uygun olarak her cookie için ayrı header
        response.addHeader("Set-Cookie", cookieString);
        
        // CORS header'ları - çapraz köken isteklerini açıkça izin ver
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
        response.setHeader("Access-Control-Expose-Headers", "Set-Cookie");
        
        // Ön-uçuş istekleri için CORS ön bellekleme süresi
        response.setHeader("Access-Control-Max-Age", "3600");
    }
}
