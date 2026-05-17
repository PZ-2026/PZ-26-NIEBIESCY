package com.vectorpeaks.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

/**
 * Initializes the Firebase Admin SDK on application startup.
 * Reads credentials from the service account JSON file on the classpath.
 */
@Configuration
public class FirebaseConfig {

    /**
     * Initializes FirebaseApp with credentials from the service account file.
     * Called automatically after the bean is created.
     *
     * @throws Exception if the credentials file cannot be read
     */
    @PostConstruct
    public void initialize() throws Exception {
        InputStream serviceAccount = getClass()
                .getClassLoader()
                .getResourceAsStream("firebase-service-account.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}