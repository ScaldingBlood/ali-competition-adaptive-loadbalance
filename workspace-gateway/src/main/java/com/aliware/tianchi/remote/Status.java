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
        upperBound = (int)(maxNum * 0.8);
        lowerBound = (int)(maxNum * 0.4);
        sum = new AtomicInteger();
//        left = new AtomicInteger(sum -1);
        state = StateEnum.HUNGRY;
    }

    public synchronized boolean increaseSize() {
        if(upperBound + 0.06 * maxNum <= maxNum) {
            upperBound += (0.06 * maxNum);
            check();
            return true;
        }
        return false;
    }

    public synchronized boolean decreaseSize() {
        if(upperBound - 0.06 * maxNum > lowerBound) {
            upperBound -= (0.06 * maxNum);
            check();
            return true;
        }
        return false;
    }
//
//    public int getAvailableCnt() {
//        return left.get();
//    }

    public int getLeft() {
        return maxNum - sum.get();
    }

    public boolean acquire() {
//        left.decrementAndGet();
    if (sum.get() >= maxNum)
        return false;
        sum.incrementAndGet();
        check();
        return true;
    }

    public void release() {
//        left.incrementAndGet();
        sum.decrementAndGet();
        check();
    }

    public void notify(double duration) {
//        if(state.compareTo(StateEnum.LIMIT) == 0)
//            if(duration <= curDuration * 1.1)
//                increaseSize();
//            else if(duration >= curDuration * 1.7)
//                decreaseSize();
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

    public int getUpperBound() {
        return upperBound;
    }
}
