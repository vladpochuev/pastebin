package com.vladpochuev.service;

import com.vladpochuev.model.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Finding the nearest empty space starting from the center")
class BFSTest {
    BFS<PlaceableTestImpl> bfs;

    @BeforeEach
    void init() {
        bfs = new BFS<>(5, 5);
    }

    @Test
    @DisplayName("Standard work of the class")
    void testNearestPoint1() {
        bfs.add(new PlaceableTestImpl(0, 0));
        bfs.add(new PlaceableTestImpl(0, 1));
        bfs.add(new PlaceableTestImpl(0, -1));
        bfs.add(new PlaceableTestImpl(-1, 0));

        Point nearest = bfs.findNearest();
        assertThat(nearest, equalTo(new Point(1, 0)));
    }

    @Test
    @DisplayName("Should put and return (0,0) if it was removed")
    void testNearestPoint2() {
        bfs.add(new PlaceableTestImpl(0, 0));
        bfs.add(new PlaceableTestImpl(1, 0));
        bfs.add(new PlaceableTestImpl(-1, 0));
        bfs.remove(new PlaceableTestImpl(0, 0));

        Point nearest = bfs.findNearest();
        assertThat(nearest, equalTo(new Point(0, 0)));
    }

    @Test
    @DisplayName("Should put and return (0,0) if the field was cleared")
    void testNearestPoint3() {
        bfs.add(new PlaceableTestImpl(0, 0));
        bfs.add(new PlaceableTestImpl(1, 0));
        bfs.clearField();

        Point nearest = bfs.findNearest();
        assertThat(nearest, equalTo(new Point(0, 0)));
    }

    @Test
    @DisplayName("Should clear the field after resizing it")
    void testNearestPoint4() {
        bfs.add(new PlaceableTestImpl(0, 0));
        bfs.setAmountOfCellsX(10);

        Point nearest = bfs.findNearest();
        assertThat(nearest, equalTo(new Point(0, 0)));
    }
}