package com.aliware.tianchi.remote;

import com.aliware.tianchi.CallbackListenerImpl;
import com.aliware.tianchi.util.ScalableSemaphore;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Status {
    public static int BATCH_SIZE = 100;
    public static int DELTA_SIZE = 25;
    private static double DELTA_CNT = DELTA_SIZE / BATCH_SIZE;
//    private static final Logger LOGGER = LoggerFactory.getLogger(Status.class);

    private double sum;
    private int maxNum;
    private ScalableSemaphore left;
//    private volatile double cnt;

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
        left = new ScalableSemaphore((int)this.sum * BATCH_SIZE);
    }

    public synchronized void increaseSize() {
        if(sum < maxNum) {
            sum += DELTA_CNT;
            left.increasePermits(DELTA_SIZE);
        }
    }

    public synchronized void decreaseSize() {
        if(sum > 1) {
            sum -= DELTA_CNT;
            left.reducePermitsInternal(DELTA_SIZE);
        }
    }

    public int getCnt() {
        return left.availablePermits();
    }

    public double getSum() {
        return sum;
    }

    public synchronized void decreaseCut(double duration) {
        curDuration = duration;
        if(duration > avgDuration * 1.2) {
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
//        System.out.println("DURATION: " + name + " this: " + duration + " avg: " + avgDuration + " cnt: " + cnt + " sum: " + sum + " debug " + debugInfo.get());
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
        queue.sort();
    }

    public double getCurDuration() {
        return curDuration;
    }
}
