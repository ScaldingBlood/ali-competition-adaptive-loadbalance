package com.aliware.tianchi.remote;

import com.aliware.tianchi.util.ScalableSemaphore;

public class Status {
    public static Integer BATCH_SIZE = 80;
    public static final int DELTA_SIZE = 25;
    public static final double THRESHOLD = 1.3;

    private int sum;
    private int maxNum;
    private ScalableSemaphore left;
    private double countDown;

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
        sum = maxNum / 2;
        left = new ScalableSemaphore(sum);
        countDown = sum / BATCH_SIZE;
    }

    public synchronized boolean increaseSize(int size) {
        size = sum + size <= maxNum ? size : maxNum - sum;
        if(size > 0) {
            sum += size;
            left.increasePermits(size);
            return true;
        }
        return false;
    }

    public synchronized boolean decreaseSize(int size) {
        size = sum - size >= BATCH_SIZE ? size : sum - BATCH_SIZE;
        if(size > 0) {
            sum -= size;
            left.reducePermitsInternal(size);
            return true;
        }
        return false;
    }

    public int getCnt() {
        return left.availablePermits();
    }

    public int getSum() {
        return sum;
    }

    public synchronized void adjustPermits(double duration) {
        if(lastDuration != 0) {
            if (duration > lastDuration * THRESHOLD) {
                decreaseSize(DELTA_SIZE);
            } else {
                increaseSize(DELTA_SIZE);
            }
        }
        lastDuration = duration;
//        System.out.println("DURATION: " + name + " this: " + duration + " avg: " + lastDuration + " sum: " + sum);
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
        if(countDown <= -1)
            adjustPermits(duration);
        else
            countDown = sum / BATCH_SIZE;
        queue.sort();
    }

    public double getCurDuration() {
        return curDuration;
    }
}
