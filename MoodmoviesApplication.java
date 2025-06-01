package com.example.moodmovies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MoodmoviesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoodmoviesApplication.class, args);
	}

}
