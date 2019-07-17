package com.aliware.tianchi.remote;

import com.aliware.tianchi.util.ScalableSemaphore;

public class Status {
    public static final int DELTA_SIZE = 10;

    private int sum;
    private int maxNum;
    private ScalableSemaphore left;

    private volatile double curDuration = 0;

    private String name;

    public Status(String name) {
        this.name = name;
    }

    public void init() {
        maxNum = Access.maxAvailableThreads.get(name);
        System.out.println(name + maxNum);
        sum = (int)(Math.ceil(maxNum / 1300.0 * 1024));
        left = new ScalableSemaphore(sum -1);
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
        size = sum - size >= 20 ? size : sum - 20;
        if(size > 0) {
            sum -= size;
            left.reducePermitsInternal(size);
            return true;
        }
        return false;
    }

    public int getAvailableCnt() {
        return left.availablePermits();
    }

    public int getSum() {
        return sum;
    }

    public void acquire() {
        try {
            left.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        left.release();
    }

    public void notify(double duration) {
        curDuration = duration;
    }

    public double getCurDuration() {
        return curDuration;
    }
}
