package com.vladpochuev.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.vladpochuev.model.DbProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class FirebaseInitialization {
    private final DbProperties dbProperties;

    public FirebaseInitialization(DbProperties dbProperties) {
        this.dbProperties = dbProperties;
    }

    @PostConstruct
    public void initialization() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream(dbProperties.getFirebaseKeyLocation());
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);

    }
}
