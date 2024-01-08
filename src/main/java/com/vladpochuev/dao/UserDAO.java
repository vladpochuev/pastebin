package com.vladpochuev.dao;

import com.vladpochuev.model.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDAO implements DAO<User> {
    private final JdbcTemplate jdbcTemplate;

    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(User user) {
        this.jdbcTemplate.update("""
                    INSERT INTO users (id, username, password)
                    VALUES (?, ?, ?)
                """, user.getId(), user.getUsername(), user.getPassword());
    }

    @Override
    public List<User> read() {
        return this.jdbcTemplate.query("SELECT * FROM users",
                new BeanPropertyRowMapper<>(User.class));
    }

    @Override
    public void update(User user) {
        this.jdbcTemplate.update("UPDATE users SET username=?, password=? WHERE id=?",
                user.getUsername(), user.getPassword(), user.getId());
    }

    @Override
    public void delete(String id) {
        this.jdbcTemplate.update("DELETE FROM bin WHERE id=?", id);
    }
}
