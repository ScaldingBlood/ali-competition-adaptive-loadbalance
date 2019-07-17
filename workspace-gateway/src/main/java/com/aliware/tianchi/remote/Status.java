package com.aliware.tianchi.remote;

import com.aliware.tianchi.util.ScalableSemaphore;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class Status {
    public static int BATCH_SIZE = 100;
    public static int DELTA_SIZE = 30;
    private static int DELTA_CNT = DELTA_SIZE / BATCH_SIZE;

    private int sum;
    private int maxNum;
    private AtomicInteger left;

    private double lastDuration = 0;
    private double avgDuration = 0;
    private volatile double curDuration = 0;

    private InvokerQueue queue;
    private String name;


    public Status(InvokerQueue queue, String name) {
        this.queue = queue;
        this.name = name;
    }

    public void init() {
        maxNum = (Access.maxAvailableThreads.get(name)) / BATCH_SIZE;
        this.sum = maxNum;
        left = new AtomicInteger(this.sum * BATCH_SIZE);
    }

    public synchronized void increaseSize() {
        if (sum + DELTA_CNT <= maxNum) {
            sum += DELTA_CNT;
            left.getAndAdd(DELTA_SIZE);
        }
    }

    public synchronized void decreaseSize() {
        if (sum - DELTA_CNT >= BATCH_SIZE) {
            sum -= DELTA_CNT;
            left.getAndAdd(-DELTA_SIZE);
        }
    }

    public int getCnt() {
        return left.get();
    }

    public synchronized void decreaseCut(double duration) {
        curDuration = duration;
        if (duration > avgDuration * 1.2 && avgDuration != 0) {
            decreaseSize();
            lastDuration = duration;
            avgDuration = duration;
        } else {
            avgDuration = lastDuration;
            lastDuration = duration;
            if (avgDuration > duration * 1.2 || (duration < Collections.max(Access.getDuration()))) {
                increaseSize();
            }
        }
//        System.out.println("DURATION: " + name + " this: " + duration + " avg: " + avgDuration + " sum: " + sum);
    }

    public void acquire() {
        left.getAndAdd(-1);

    }

    public void release(double duration) {
        left.getAndAdd(BATCH_SIZE);
        curDuration = duration;
//        decreaseCut(duration);
        queue.sort();
    }

    public double getCurDuration() {
        return curDuration;
    }
}
