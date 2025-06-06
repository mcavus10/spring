package com.example.moodmovies.security;

import com.example.moodmovies.security.UserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final CookieService cookieService;

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String defaultRedirectUri;

    // Bu repository, OAuth akışı başladığında orijinal isteği (redirect_uri dahil) session'da saklar.
    // Bu sayede başarılı login sonrası nereye döneceğimizi biliriz.
    private final HttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Orijinal isteği session'dan alarak hedef URL'yi belirle
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);

        // JWT token'larını oluştur
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getId());

        // Web istemcileri için cookie'leri her zaman set et
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(refreshToken).toString());
        log.debug("JWT and Refresh Token cookies set for the response.");

        // Mobil bir redirect URI ise (custom scheme içeriyorsa) token'ları query'ye ekle
        if (targetUrl.startsWith("moodiemovies://")) {
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("token", accessToken)
                    // .queryParam("refresh_token", refreshToken) // Genellikle sadece access token yeterli
                    .build().toUriString();
            
            log.info("OAuth2 Success: Redirecting to mobile custom URI with token in query param.");
        } else {
            // Web için token'ları URL'e eklemeden, sadece cookie'lerle yönlendir
            log.info("OAuth2 Success: Redirecting to web URI. Tokens are in cookies.");
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        // Orijinal isteği session'dan temizle
        authorizationRequestRepository.removeAuthorizationRequest(request, response);
    }
    
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Orijinal istekteki redirect_uri parametresini al
        Optional<String> redirectUri = Optional.ofNullable(request.getParameter("redirect_uri"));
        
        // Eğer istekte bir redirect_uri belirtilmişse onu kullan, yoksa application.properties'teki varsayılanı kullan
        String targetUrl = redirectUri.orElse(defaultRedirectUri);
        log.debug("Determined target URL for OAuth2 success: {}", targetUrl);
        return targetUrl;
    }
}