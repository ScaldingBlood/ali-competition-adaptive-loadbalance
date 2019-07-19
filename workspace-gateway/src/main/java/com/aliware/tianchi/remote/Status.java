package com.aliware.tianchi.remote;

import java.util.concurrent.atomic.AtomicInteger;

import com.aliware.tianchi.util.ScalableSemaphore;

public class Status {
    public static final int DELTA_SIZE = 10;

    private int sum;
    private int maxNum;
    private AtomicInteger left;

    private volatile double curDuration = 0;

    private String name;

    public Status(String name) {
        this.name = name;
    }

    public void init() {
        maxNum = Access.maxAvailableThreads.get(name);
        System.out.println(name + maxNum);
        sum = (int)(Math.ceil(maxNum / 1300.0 * 1024));
        left = new AtomicInteger(sum -1);
    }

    public synchronized boolean increaseSize(int size) {
        if(size + sum <= maxNum) {
            sum += size;
            left.addAndGet(size);
            return true;
        }
        return false;
    }

    public synchronized boolean decreaseSize(int size) {
        if(sum - size > 20) {
            sum -= size;
            left.addAndGet(-size);
            return true;
        }
        return false;
    }

    public int getAvailableCnt() {
        return left.get();
    }

    public int getSum() {
        return sum;
    }

    public void acquire() {
        left.decrementAndGet();
    }

    public void release() {
        left.incrementAndGet();
    }

    public void notify(double duration) {
        curDuration = duration;
    }

    public double getCurDuration() {
        return curDuration;
    }
}
