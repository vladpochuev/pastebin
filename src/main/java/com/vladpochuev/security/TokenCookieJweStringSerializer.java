package com.vladpochuev.security;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.function.Function;

public class TokenCookieJweStringSerializer implements Function<Token, String> {
    private final JWEEncrypter jweEncrypter;
    private JWEAlgorithm jweAlgorithm = JWEAlgorithm.DIR;
    private EncryptionMethod encryptionMethod = EncryptionMethod.A128GCM;
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenCookieJweStringSerializer.class);

    public TokenCookieJweStringSerializer(JWEEncrypter jweEncrypter) {
        this.jweEncrypter = jweEncrypter;
    }

    public TokenCookieJweStringSerializer(JWEEncrypter jweEncrypter, JWEAlgorithm jweAlgorithm, EncryptionMethod encryptionMethod) {
        this.jweEncrypter = jweEncrypter;
        this.jweAlgorithm = jweAlgorithm;
        this.encryptionMethod = encryptionMethod;
    }

    @Override
    public String apply(Token token) {
        JWEHeader jweHeader = new JWEHeader.Builder(this.jweAlgorithm, this.encryptionMethod)
                .keyID(token.id().toString())
                .build();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .jwtID(token.id().toString())
                .subject(token.subject())
                .issueTime(Date.from(token.createdAt()))
                .expirationTime(Date.from(token.expiresAt()))
                .claim("authorities", token.authorities())
                .build();
        EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, claimsSet);
        try {
            encryptedJWT.encrypt(this.jweEncrypter);
            return encryptedJWT.serialize();
        } catch (JOSEException exception) {
            LOGGER.error(exception.getMessage(), exception);
            return null;
        }
    }
}
