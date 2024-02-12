package com.vladpochuev.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGenerator {
    private Object[] data;

    public HashGenerator(Object... data) {
        updateDataForHashing(data);
    }

    public void updateDataForHashing(Object... values) {
        this.data = values;
    }

    public String getHash(String algorithm, int length) {
        try {
            String stringValues = joinValues(data);
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            byte[] bytes = messageDigest.digest(stringValues.getBytes());
            return convertToString(bytes).substring(0, length);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String joinValues(Object... values) {
        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            builder.append(value.toString());
        }
        return builder.toString();
    }

    private String convertToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public Object[] getData() {
        return data;
    }
}
