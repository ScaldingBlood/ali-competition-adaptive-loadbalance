package com.aliware.tianchi.remote;

import java.util.concurrent.atomic.AtomicInteger;

import com.aliware.tianchi.util.ScalableSemaphore;

public class Status {
    public static final int DELTA_SIZE = 10;

    private AtomicInteger sum;
    private int maxNum;
    private int upperBound;
    private int lowerBound;
    private AtomicInteger left;
    private StateEnum state;

    private volatile double curDuration = 0;

    private String name;

    public Status(String name) {
        this.name = name;
    }

    public void init() {
        maxNum = Access.maxAvailableThreads.get(name);
        System.out.println(name + maxNum);
        upperBound = (int)(maxNum * 0.86);
        lowerBound = (int)(maxNum * 0.7);
        sum = new AtomicInteger();
//        left = new AtomicInteger(sum -1);
        state = StateEnum.HUNGRY;
    }
//
//    public synchronized boolean increaseSize(int size) {
//        if(size + sum <= maxNum) {
//            sum += size;
//            left.addAndGet(size);
//            if(sum > upperBound)
//                state = StateEnum.LIMIT;
//            return true;
//        }
//        return false;
//    }
//
//    public synchronized boolean decreaseSize(int size) {
//        if(sum - size > 20) {
//            sum -= size;
//            left.addAndGet(-size);
//            if(sum < lowerBound)
//                state = StateEnum.HUNGRY;
//            return true;
//        }
//        return false;
//    }
//
//    public int getAvailableCnt() {
//        return left.get();
//    }

    public int getLeft() {
        return maxNum - sum.get();
    }

    public void acquire(int n) {
//        left.decrementAndGet();
        while(sum.get() + n >= maxNum);
        sum.addAndGet(n);
        check();
    }

    public void release() {
//        left.incrementAndGet();
        sum.decrementAndGet();
        check();
    }

    public void notify(double duration) {
        curDuration = duration;
    }

    public double getCurDuration() {
        return curDuration;
    }

    public void check() {
        if(sum.get() > upperBound)
            state = StateEnum.LIMIT;
        else if(sum.get() < lowerBound)
            state = StateEnum.HUNGRY;
        else
            state = StateEnum.NORMAL;
    }

    public StateEnum getState() {
        return state;
    }

    public String getName() {
        return name;
    }
}
