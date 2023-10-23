package com.vladpochuev.dao;

import com.vladpochuev.model.Bin;
import com.vladpochuev.model.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BinDAO implements DAO<Bin> {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BinDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public void create(Bin bin) {
        jdbcTemplate.update("INSERT INTO bin(id, title, message, x, y) VALUES(?, ?, ?, ?, ?)", bin.getId(),
                bin.getTitle(), bin.getMessage(), bin.getX(), bin.getY());
    }
    @Override
    public Bin readById(String id) {
        return jdbcTemplate.query("SELECT * FROM bin WHERE id=?",
                new BeanPropertyRowMapper<>(Bin.class), id).stream().findAny().orElse(null);
    }

    @Override
    public Bin readByCoords(Point point) {
        return jdbcTemplate.query("SELECT * FROM bin WHERE x=? AND y=?",
                new BeanPropertyRowMapper<>(Bin.class), point.getX(), point.getY()).stream().findAny().orElse(null);
    }

    @Override
    public List<Bin> readAll() {
        return jdbcTemplate.query("SELECT * FROM bin", new BeanPropertyRowMapper<>(Bin.class));
    }

    @Override
    public void update(Bin bin) {
        jdbcTemplate.update("UPDATE bin SET title=?, message=?, x=?, y=? WHERE id=?", bin.getTitle(),
                bin.getMessage(), bin.getX(), bin.getY(), bin.getId());
    }

    @Override
    public void delete(String id) {
        jdbcTemplate.update("DELETE FROM bin WHERE id=?", id);
    }
}
