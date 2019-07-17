package com.aliware.tianchi.remote;

import com.aliware.tianchi.util.ScalableSemaphore;
import java.util.Collections;

public class Status {
    public static double BATCH_SIZE = 100;
    public static double DELTA_SIZE = 30;
    private static double DELTA_CNT = DELTA_SIZE / BATCH_SIZE;

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
        this.sum = maxNum;
        left = new ScalableSemaphore(this.sum * BATCH_SIZE);
    }

    public synchronized void increaseSize() {
        if(sum + DELTA_CNT <= maxNum) {
            sum += DELTA_CNT;
            left.increasePermits(DELTA_SIZE);
        }
    }

    public synchronized void decreaseSize() {
        if(sum - DELTA_CNT >= BATCH_SIZE) {
            sum -= DELTA_CNT;
            left.reducePermitsInternal(DELTA_SIZE);
        }
    }

    public int getCnt() {
        return left.availablePermits();
    }

    public synchronized void decreaseCut(double duration) {
        curDuration = duration;
        if(duration > avgDuration * 1.2 && avgDuration != 0) {
            decreaseSize();
            lastDuration = duration;
            avgDuration = duration;
        } else {
            avgDuration = lastDuration;
            lastDuration = duration;
            if(avgDuration > duration * 1.2 || (duration < Collections.max(Access.getDuration()))) {
                increaseSize();
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
//        decreaseCut(duration);
        queue.sort();
    }

    public double getCurDuration() {
        return curDuration;
    }
}
