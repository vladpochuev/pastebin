package com.vladpochuev.service;

import com.google.api.core.ApiFutureCallback;
import com.google.cloud.firestore.WriteResult;
import com.vladpochuev.config.PastebinApplication;
import com.vladpochuev.model.FirestoreMessageEntity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.MethodOrderer.*;

@SpringBootTest(classes = PastebinApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Firestore Database CRUD methods")
class FirestoreMessageServiceTest {
    FirestoreMessageService firestoreMessageService;
    static String key;
    static String message;

    public FirestoreMessageServiceTest(FirestoreMessageService firestoreMessageService) {
        this.firestoreMessageService = firestoreMessageService;
    }

    @BeforeAll
    void init() {
        key = UUID.randomUUID().toString();
        message = "test";
    }

    @Test
    @Order(1)
    @DisplayName("Should be able to create message")
    void testCreateMessage() {
        FirestoreMessageEntity entity = new FirestoreMessageEntity(key, message);
        ApiFutureCallback<WriteResult> callback = new ApiFutureCallback<>() {
            @Override
            public void onFailure(Throwable throwable) {
                fail(throwable.getMessage());
            }

            @Override
            public void onSuccess(WriteResult writeResult) {
                assertThat(writeResult, notNullValue());
            }
        };

        firestoreMessageService.sendCreate(entity, callback);
    }

    @Test
    @Order(2)
    @DisplayName("Should be able to read message")
    void testReadMessage() throws ExecutionException, InterruptedException {
        FirestoreMessageEntity entity = firestoreMessageService.read(key);
        assertThat(entity, notNullValue());
        assertThat(entity.getMessage(), equalTo(message));
        assertThat(entity.getUUID(), equalTo(key));
    }

    @Test
    @Order(3)
    @DisplayName("Should be able to delete message")
    void testDeleteMessage() {
        ApiFutureCallback<WriteResult> callback = new ApiFutureCallback<>() {
            @Override
            public void onFailure(Throwable throwable) {
                fail(throwable.getMessage());
            }

            @Override
            public void onSuccess(WriteResult writeResult) {
                assertThat(writeResult, notNullValue());
            }
        };

        firestoreMessageService.sendDelete(key, callback);
    }
}