package com.vladpochuev.dao;

import com.vladpochuev.model.BinEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
public class BinDAO implements DAO<BinEntity> {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BinDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(BinEntity binEntity) throws DuplicateKeyException {
        Timestamp timestamp = binEntity.getExpirationTime() == null ? null : Timestamp.valueOf(binEntity.getExpirationTime());
        jdbcTemplate.update("""
                                    BEGIN;
                                    LOCK TABLE bin IN EXCLUSIVE MODE;
                                    INSERT INTO bin(id, title, messageUUID, x, y, color,
                                     expirationTime, amountOfTime, username)
                                    VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);
                                    COMMIT;
        """, binEntity.getId(), binEntity.getTitle(), binEntity.getMessageUUID(), binEntity.getX(), binEntity.getY(),
                binEntity.getColor(), timestamp, binEntity.getAmountOfTime(), binEntity.getUsername());
    }

    @Override
    public List<BinEntity> read() {
        return jdbcTemplate.query("SELECT * FROM bin",
                new BeanPropertyRowMapper<>(BinEntity.class));
    }

    public List<BinEntity> readExpired() {return jdbcTemplate.query("SELECT * FROM bin WHERE expirationTime < NOW() ",
            new BeanPropertyRowMapper<>(BinEntity.class));}

    public BinEntity readById(String id) {
        return jdbcTemplate.query("SELECT * FROM bin WHERE id=?",
                new BeanPropertyRowMapper<>(BinEntity.class), id).stream().findAny().orElse(null);
    }

    @Override
    public void update(BinEntity binEntity) {
        jdbcTemplate.update("UPDATE bin SET title=?, messageUUID=?, color=? WHERE id=?",
                binEntity.getTitle(), binEntity.getMessageUUID(), binEntity.getColor(), binEntity.getId());
    }

    @Override
    public void delete(String id) {
        jdbcTemplate.update("DELETE FROM bin WHERE id=?", id);
    }

    public void deleteExpired() {
        jdbcTemplate.update("DELETE FROM bin WHERE expirationTime < NOW()");
    }
}
