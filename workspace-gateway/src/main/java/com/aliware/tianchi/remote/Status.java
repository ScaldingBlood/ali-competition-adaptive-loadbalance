package com.aliware.tianchi.remote;

import com.aliware.tianchi.util.ScalableSemaphore;
import java.util.Collections;

public class Status {
    public static final double BATCH_SIZE = 100;
    private static final double DELTA_SIZE = 25;
    private static final double THRESHOLD = (BATCH_SIZE + DELTA_SIZE) / BATCH_SIZE - 0.05;

    private double sum;
    private double maxNum;
    private ScalableSemaphore left;

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
        this.sum = maxNum / 2;
        left = new ScalableSemaphore(this.sum * BATCH_SIZE);
    }

    public synchronized void increaseSize(double num) {
        double rate = num / BATCH_SIZE;
        rate = sum + rate <= maxNum ? rate : maxNum - sum;
        if(rate > 0) {
            sum += rate;
            left.increasePermits(rate * BATCH_SIZE);
        }
    }

    public synchronized void decreaseSize(double num) {
        double rate = num / BATCH_SIZE;
        rate = sum - rate >= 1 ? rate : sum - 1;
        if(rate > 0) {
            sum -= rate;
            left.reducePermitsInternal(rate * BATCH_SIZE);
        }
    }

    public int getCnt() {
        return left.availablePermits();
    }

    public synchronized void decreaseCut(double duration) {
        curDuration = duration;
        if(duration > avgDuration * THRESHOLD && avgDuration != 0) {
            decreaseSize(DELTA_SIZE);
            lastDuration = duration;
            avgDuration = duration;
        } else {
            avgDuration = lastDuration;
            lastDuration = duration;
            if(avgDuration > duration * THRESHOLD || (duration < Collections.max(Access.getDuration()))) {
                increaseSize(DELTA_SIZE);
            }
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
        left.release((int)BATCH_SIZE);
        curDuration = duration;
        decreaseCut(duration);
        queue.sort();
    }

    public double getCurDuration() {
        return curDuration;
    }
}
