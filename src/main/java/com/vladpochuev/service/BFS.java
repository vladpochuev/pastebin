package com.vladpochuev.service;

import com.vladpochuev.model.Placeable;
import com.vladpochuev.model.Point;

import java.util.*;

public class BFS<Obj extends Placeable> {
    private final HashSet<Point> searched = new HashSet<>();
    private final Deque<Point> deque = new ArrayDeque<>();
    private boolean[][] field;
    private int amountOfCellsX;
    private int amountOfCellsY;

    public BFS(int amountOfCellsX, int amountOfCellsY) {
        this.amountOfCellsX = amountOfCellsX;
        this.amountOfCellsY = amountOfCellsY;
        field = new boolean[amountOfCellsX * 2 + 1][amountOfCellsY * 2 + 1];
    }

    public void add(Obj object) {
        field[object.getX() + amountOfCellsX][object.getY() + amountOfCellsY] = true;
    }

    public void remove(Obj object) {
        field[object.getX() + amountOfCellsX][object.getY() + amountOfCellsY] = false;
    }

    public void clearField() {
        field = new boolean[amountOfCellsX * 2 + 1][amountOfCellsY * 2 + 1];
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

    public int getAmountOfCellsX() {
        return amountOfCellsX;
    }

    public int getAmountOfCellsY() {
        return amountOfCellsY;
    }

    public void setAmountOfCellsX(int amountOfCellsX) {
        this.amountOfCellsX = amountOfCellsX;
        clearField();
    }

    public void setAmountOfCellsY(int amountOfCellsY) {
        this.amountOfCellsY = amountOfCellsY;
        clearField();
    }
}
