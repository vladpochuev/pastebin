package com.vladpochuev.dao;

import com.vladpochuev.model.Bin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BinDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BinDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Bin bin) {
        jdbcTemplate.update("INSERT INTO bin(id, title, message, x, y) VALUES(?, ?, ?, ?, ?)", bin.getId(),
                bin.getTitle(), bin.getMessage(), bin.getX(), bin.getY());
    }

    public Bin read(String id) {
        return jdbcTemplate.query("SELECT * FROM bin WHERE id=?",
                new BeanPropertyRowMapper<>(Bin.class), id).stream().findAny().orElse(null);
    }

    public List<Bin> readAll() {
        return jdbcTemplate.query("SELECT * FROM bin", new BeanPropertyRowMapper<>(Bin.class));
    }

    public void update(Bin bin) {
        jdbcTemplate.update("UPDATE bin SET title=?, message=?, x=?, y=? WHERE id=?", bin.getTitle(),
                bin.getMessage(), bin.getX(), bin.getY(), bin.getId());
    }

    public void delete(String id) {
        jdbcTemplate.update("DELETE FROM bin WHERE id=?", id);
    }
}
