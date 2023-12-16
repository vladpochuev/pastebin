package com.vladpochuev.dao;

import com.vladpochuev.model.Bin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
public class BinDAO implements DAO<Bin> {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BinDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Bin bin) throws DuplicateKeyException {
        Timestamp timestamp = bin.getExpirationTime() == null ? null : Timestamp.valueOf(bin.getExpirationTime());
        jdbcTemplate.update("""
                                    BEGIN;
                                    LOCK TABLE bin IN EXCLUSIVE MODE;
                                    INSERT INTO bin(id, title, message, x, y, color,
                                     expirationTime, amountOfTime, username)
                                    VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);
                                    COMMIT;
        """, bin.getId(), bin.getTitle(), bin.getMessage(), bin.getX(), bin.getY(),
                bin.getColor(), timestamp, bin.getAmountOfTime(), bin.getUsername());
    }

    @Override
    public List<Bin> read() {
        return jdbcTemplate.query("SELECT * FROM bin",
                new BeanPropertyRowMapper<>(Bin.class));
    }

    public List<Bin> readExpired() {return jdbcTemplate.query("SELECT * FROM bin WHERE expirationTime < NOW() ",
            new BeanPropertyRowMapper<>(Bin.class));}

    public Bin readById(String id) {
        return jdbcTemplate.query("SELECT * FROM bin WHERE id=?",
                new BeanPropertyRowMapper<>(Bin.class), id).stream().findAny().orElse(null);
    }

    @Override
    public void update(Bin bin) {
        jdbcTemplate.update("UPDATE bin SET title=?, message=?, color=? WHERE id=?",
                bin.getTitle(), bin.getMessage(), bin.getColor(), bin.getId());
    }

    @Override
    public void delete(String id) {
        jdbcTemplate.update("DELETE FROM bin WHERE id=?", id);
    }

    public void deleteExpired() {
        jdbcTemplate.update("DELETE FROM bin WHERE expirationTime < NOW()");
    }
}
