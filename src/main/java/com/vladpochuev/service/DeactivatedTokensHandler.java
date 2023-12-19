package com.vladpochuev.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DeactivatedTokensHandler {
    private final JdbcTemplate jdbcTemplate;

    public DeactivatedTokensHandler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedDelay = 60000)
    public void deleteExpired() {
        jdbcTemplate.update("DELETE FROM deactivated_tokens WHERE expirationTime < NOW()");
    }
}
