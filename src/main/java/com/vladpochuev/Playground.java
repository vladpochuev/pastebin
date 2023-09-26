package com.vladpochuev;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class Playground {
    public static void main(String[] args) {
        var counter = new AtomicInteger();
        while (true) {
            new Thread(() -> {
                int count = counter.incrementAndGet();
                System.out.println("thread count = " + count);
            }).start();
        }
    }
}