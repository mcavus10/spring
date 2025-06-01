package com.example.moodmovies.service.oauth2;

import com.example.moodmovies.service.OAuth2UserInfo;
import java.util.Map;

/**
 * Facebook kullanıcı bilgilerini işlemek için özel sınıf.
 * Bu sınıf doğrudan kullanılmaz, OAuth2UserServiceImpl içinde
 * Facebook yanıtlarını işlemek için kullanılır.
 */
public class FacebookOAuth2UserInfo {
    
    /**
     * Facebook OAuth2 yanıtından kullanıcı bilgilerini çıkarır
     * 
     * @param attributes Facebook yanıtı
     * @return OAuth2UserInfo nesnesi
     */
    public static OAuth2UserInfo extract(Map<String, Object> attributes) {
        String id = (String) attributes.get("id");
        String name = (String) attributes.get("name");
        String email = (String) attributes.get("email");
        
        // Resim bilgilerini kullanmıyoruz, null olarak bırakıyoruz
        String imageUrl = null;
        
        return new OAuth2UserInfo(attributes, id, name, email, imageUrl);
    }
}
