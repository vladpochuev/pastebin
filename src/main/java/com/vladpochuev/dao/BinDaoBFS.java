package com.vladpochuev.dao;

import com.vladpochuev.model.BinEntity;
import com.vladpochuev.model.Point;
import com.vladpochuev.service.BFS;

import java.util.*;

public class BinDaoBFS {
    private final DAO<BinEntity> dao;
    private final BFS<BinEntity> bfs = new BFS<>(100, 100);

    public BinDaoBFS(DAO<BinEntity> dao) {
        this.dao = dao;
    }

    public Point findNearest() {
        return bfs.findNearest();
    }

    public void fillField() {
        List<BinEntity> bins = dao.read();
        for (BinEntity bin : bins) {
            bfs.add(bin);
        }
    }
}
