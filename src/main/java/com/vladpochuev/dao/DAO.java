package com.vladpochuev.dao;

import com.vladpochuev.model.Point;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DAO<Obj> {
    void create(Obj object);
    Obj readById(String id);
    Obj readByCoords(Point point);
    List<Obj> readAll();
    void update(Obj object);
    void delete(String id);
}
