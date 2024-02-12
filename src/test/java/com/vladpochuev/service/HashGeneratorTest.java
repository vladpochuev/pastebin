package com.vladpochuev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Generating of hashes based on input values")
class HashGeneratorTest {
    HashGenerator hashGenerator;
    static final Object[] VALUES = {"test", System.currentTimeMillis(), Math.random()};

    @BeforeEach
    void init() {
        hashGenerator = new HashGenerator(VALUES);
    }

    @Test
    @DisplayName("Should get and store correct values")
    void testIfStores() {
        Object[] receivedValues = hashGenerator.getData();
        assertThat(receivedValues, equalTo(VALUES));
    }

    @Test
    @DisplayName("Hash must have length of the passed parameter")
    void testLength() {
        int length = 16;
        String hash = hashGenerator.getHash("MD5", length);
        assertThat(hash.length(), equalTo(length));
    }

    @Test
    @DisplayName("Method updateDataForHashing(Object... values) should set inner data")
    void testUpdate() {
        hashGenerator.updateDataForHashing("test");
        assertThat(hashGenerator.getData(), equalTo(new Object[]{"test"}));
    }

    @Test
    @DisplayName("Hashes generated from the same data must be the same")
    void testIfSameData() {
        int length = 16;
        String algorithm = "SHA-512";
        String firstHash = hashGenerator.getHash(algorithm, length);
        String secondHash = hashGenerator.getHash(algorithm, length);

        assertThat(firstHash, equalTo(secondHash));
    }


    @Test
    @DisplayName("Hashes generated from different data can't be the same")
    void testIfDiffData() {
        int length = 16;
        String algorithm = "SHA-1";
        String firstHash = hashGenerator.getHash(algorithm, length);
        hashGenerator.updateDataForHashing(System.currentTimeMillis());
        String secondHash = hashGenerator.getHash(algorithm, length);

        assertThat(firstHash, not(equalTo(secondHash)));
    }

    @Test
    @DisplayName("Wrong hash name should throw exception")
    void testWrongAlgorithm() {
        assertThrows(Exception.class, () -> hashGenerator.getHash("Algorithm", 16));
    }
}