package com.aliware.tianchi.remote;

import com.aliware.tianchi.util.ScalableSemaphore;

public class Status {
    public static Integer BATCH_SIZE = 100;
    public static final int DELTA_SIZE = 10;
//    public static final double THRESHOLD = 1.25;

    private int sum;
    private int maxNum;
    private ScalableSemaphore left;

    private volatile double curDuration = 0;

    private InvokerQueue queue;
    private String name;


    public Status(InvokerQueue queue, String name) {
        this.queue = queue;
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
        size = sum - size > 50 ? size : sum - 50;
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

//    public synchronized void adjustPermits(double duration) {
//        if(lastDuration != 0) {
//            if (duration > lastDuration * THRESHOLD) {
//                decreaseSize(DELTA_SIZE);
//            } else if(lastDuration > duration * THRESHOLD) {
//                increaseSize(DELTA_SIZE);
//            }
//        }
//        lastDuration = duration;
//        System.out.println("DURATION: " + name + " this: " + duration + " avg: " + lastDuration + " sum: " + sum);
//    }

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
//        left.release(BATCH_SIZE);
        curDuration = duration;
        queue.sort();
    }

    public double getCurDuration() {
        return curDuration;
    }
}
