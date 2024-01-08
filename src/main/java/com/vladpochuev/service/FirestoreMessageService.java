package com.vladpochuev.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.vladpochuev.model.FirestoreMessageEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class FirestoreMessageService {
    private static final String COLLECTION_NAME = "messages";

    public void sendCreate(FirestoreMessageEntity entity) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        dbFirestore.collection(COLLECTION_NAME).document(entity.getUUID()).set(entity);
    }

    public FirestoreMessageEntity read(String name) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<DocumentSnapshot> documentSnapshot = dbFirestore.collection(COLLECTION_NAME).document(name).get();
        DocumentSnapshot snapshot = documentSnapshot.get();
        return snapshot.exists() ? snapshot.toObject(FirestoreMessageEntity.class) : null;
    }

    public void sendDelete(String name) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        dbFirestore.collection(COLLECTION_NAME).document(name).delete();
    }
}
