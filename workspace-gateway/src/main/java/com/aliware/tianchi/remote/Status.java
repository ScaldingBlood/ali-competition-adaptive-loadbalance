package com.aliware.tianchi.remote;

import com.aliware.tianchi.util.ScalableSemaphore;
import java.util.Collections;

public class Status {
    public static final int BATCH_SIZE = 100;
    public static final int DELTA_SIZE = 30;
    public static final double THRESHOLD = (BATCH_SIZE + DELTA_SIZE) / BATCH_SIZE - 0.05;

    private int sum;
    private int maxNum;
    private ScalableSemaphore left;

    private double lastDuration = 0;
    private volatile double curDuration = 0;

    private InvokerQueue queue;
    private String name;


    public Status(InvokerQueue queue, String name) {
        this.queue = queue;
        this.name = name;
    }

    public void init() {
        maxNum = Access.maxAvailableThreads.get(name);
        this.sum = maxNum / 2;
        left = new ScalableSemaphore(this.sum);
    }

    public synchronized void increaseSize() {
        int size = sum + DELTA_SIZE <= maxNum ? DELTA_SIZE : maxNum - sum;
        if(size > 0) {
            sum += size;
            left.increasePermits(size);
        }
    }

    public synchronized void decreaseSize() {
        int size = sum - DELTA_SIZE >= BATCH_SIZE ? DELTA_SIZE : sum - BATCH_SIZE;
        if(size > 0) {
            sum -= size;
            left.reducePermitsInternal(size);
        }
    }

    public int getCnt() {
        return left.availablePermits();
    }

    public synchronized void decreaseCut(double duration) {
        if(duration > lastDuration * THRESHOLD && lastDuration != 0) {
            decreaseSize();
        } else if(lastDuration > duration * THRESHOLD || (duration < Collections.max(Access.getDuration()))) {
            increaseSize();
        }
//        System.out.println("DURATION: " + name + " this: " + duration + " avg: " + avgDuration + " sum: " + sum);
    }

    public void acquire() {
        try {
            left.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release(double duration) {
        left.release(BATCH_SIZE);
        curDuration = duration;
        decreaseCut(duration);
        lastDuration = duration;
        queue.sort();
    }

    public double getCurDuration() {
        return curDuration;
    }
}
