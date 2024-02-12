package com.vladpochuev.service;

import com.vladpochuev.model.Placeable;

class PlaceableTestImpl implements Placeable {
    private Integer x;
    private Integer y;

    public PlaceableTestImpl(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Integer getX() {
        return x;
    }

    @Override
    public Integer getY() {
        return y;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}
