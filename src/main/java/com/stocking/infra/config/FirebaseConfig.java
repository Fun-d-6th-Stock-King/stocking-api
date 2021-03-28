package com.stocking.infra.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FirebaseConfig {

    private static final String FIREBASE_CONFIG_PATH = "alert-billing-firebase-adminsdk-y2m7t-297952e15c.json";

    @Bean
    public void initailize() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) return;
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(
                GoogleCredentials.fromStream(new ClassPathResource(FIREBASE_CONFIG_PATH).getInputStream()))
            .build();
        FirebaseApp defaultApp = FirebaseApp.initializeApp(options);
        log.info("Firebase application has been initialized, NAME : " + defaultApp.getName());
    }
}
