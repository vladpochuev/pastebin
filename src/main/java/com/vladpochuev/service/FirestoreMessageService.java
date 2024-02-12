package com.vladpochuev.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.google.protobuf.Api;
import com.vladpochuev.model.FirestoreMessageEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class FirestoreMessageService {
    private static final String COLLECTION_NAME = "messages";

    public void sendCreate(FirestoreMessageEntity entity) {
        sendCreate(entity, null);
    }

    public void sendCreate(FirestoreMessageEntity entity, ApiFutureCallback<WriteResult> callback) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> future = dbFirestore.collection(COLLECTION_NAME).document(entity.getUUID()).set(entity);
        getWithCallback(future, callback);
    }

    public FirestoreMessageEntity read(String name) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<DocumentSnapshot> documentSnapshot = dbFirestore.collection(COLLECTION_NAME).document(name).get();
        DocumentSnapshot snapshot = documentSnapshot.get();
        return snapshot.exists() ? snapshot.toObject(FirestoreMessageEntity.class) : null;
    }

    public void sendDelete(String name) {
        sendDelete(name, null);
    }

    public void sendDelete(String name, ApiFutureCallback<WriteResult> callback) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> future = dbFirestore.collection(COLLECTION_NAME).document(name).delete();
        getWithCallback(future, callback);
    }

    private void getWithCallback(ApiFuture<WriteResult> future, ApiFutureCallback<WriteResult> callback) {
        try {
            if (callback != null) {
                ApiFutures.addCallback(future, callback, Runnable::run);
            }
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
