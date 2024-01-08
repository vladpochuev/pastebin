package com.vladpochuev.dao;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DAO<Obj> {
    void create(Obj object);

    List<Obj> read();

    void update(Obj object);

    void delete(String id);
}
