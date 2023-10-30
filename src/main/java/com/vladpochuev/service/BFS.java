package com.vladpochuev.service;

import com.vladpochuev.dao.DAO;
import com.vladpochuev.model.Placeable;
import com.vladpochuev.model.Point;

import java.util.*;

public class BFS<Obj extends Placeable> {
    private final HashSet<Point> searched = new HashSet<>();
    private boolean[][] field;
    private final Deque<Point> deque = new ArrayDeque<>();
    private final DAO<Obj> dao;

    private final int amountOfCellsX;
    private final int amountOfCellsY;

    public BFS(DAO<Obj> dao, int amountOfCellsX, int amountOfCellsY) {
        this.dao = dao;
        this.amountOfCellsX = amountOfCellsX;
        this.amountOfCellsY = amountOfCellsY;
        fillField();
    }

    public Point findNearest() {
        Point curPoint = new Point(0, 0);

        while (field[curPoint.getX() + amountOfCellsX][curPoint.getY() + amountOfCellsY]) {
            addNearbyPoints(curPoint);
            curPoint = deque.pop();
        }

        field[curPoint.getX() + amountOfCellsX][curPoint.getY() + amountOfCellsY] = true;
        return curPoint;
    }

    private void fillField() {
        field = new boolean[amountOfCellsX * 2 + 1][amountOfCellsY * 2 + 1];
        List<Obj> objects = dao.readAll();
        for (Obj object : objects) {
            field[object.getX() + amountOfCellsX][object.getY() + amountOfCellsY] = true;
        }
    }

    private void addNearbyPoints(Point curPoint) {
        int x = curPoint.getX();
        int y = curPoint.getY();
        List<Point> nearbyPoints = List.of(
                new Point(x, y + 1), new Point(x, y - 1), new Point(x + 1, y),
                new Point(x - 1, y), new Point(x + 1, y + 1), new Point(x + 1, y - 1),
                new Point(x - 1, y + 1), new Point(x - 1, y - 1));
        for (Point nearbyPoint : nearbyPoints) {
            if (!searched.contains(nearbyPoint)) {
                searched.add(nearbyPoint);
                deque.add(nearbyPoint);
            }
        }
    }
}
