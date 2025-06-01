package com.example.moodmovies.service;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuth2UserInfo {
    private Map<String, Object> attributes;
    private String providerId;
    private String name;
    private String email;
    private String imageUrl;
}
