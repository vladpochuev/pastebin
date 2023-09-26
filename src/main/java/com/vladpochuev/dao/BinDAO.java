package com.vladpochuev.dao;

import com.vladpochuev.model.Bin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BinDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BinDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Bin bin) {
        jdbcTemplate.update("INSERT INTO bin(id, title, text) VALUES(?, ?, ?)", bin.getId(), bin.getTitle(), bin.getText());
    }

    public Bin read(String id) {
        return jdbcTemplate.query("SELECT * FROM bin WHERE id=?",
                new BeanPropertyRowMapper<>(Bin.class), id).stream().findAny().orElse(null);
    }

    public void update(Bin bin) {
        jdbcTemplate.update("UPDATE bin SET title=?, text=? WHERE id=?", bin.getTitle(), bin.getText(), bin.getId());
    }

    public void delete(String id) {
        jdbcTemplate.update("DELETE FROM bin WHERE id=?", id);
    }
}
