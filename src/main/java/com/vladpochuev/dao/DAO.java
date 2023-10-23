package com.vladpochuev.dao;

import com.vladpochuev.model.Point;

import java.util.List;

public interface DAO<Obj> {
    void create(Obj object);
    Obj readById(String id);
    Obj readByCoords(Point point);
    List<Obj> readAll();
    void update(Obj object);
    void delete(String id);
}
