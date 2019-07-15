package com.aliware.tianchi.remote;

import com.aliware.tianchi.util.ScalableSemaphore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Status {
    public static Map<String, Integer> BATCH_SIZE_MAP = new HashMap<>();
    public static final int DELTA_SIZE = 25;
    public static final double THRESHOLD = 1.6;

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
        sum = maxNum / 2;
        left = new ScalableSemaphore(sum);
        BATCH_SIZE_MAP.put(name, sum);
    }

    public synchronized void increaseSize(int size) {
        size = sum + size <= maxNum ? size : maxNum - sum;
        if(size > 0) {
            sum += size;
            left.increasePermits(size);
        }
    }

    public synchronized void decreaseSize(int size) {
        int batch_size = BATCH_SIZE_MAP.get(name);
        size = sum - size >= batch_size ? size : sum - batch_size;
        if(size > 0) {
            sum -= size;
            left.reducePermitsInternal(size);
        }
    }

    public int getCnt() {
        return left.availablePermits();
    }

    public int getSum() {
        return sum;
    }

    public synchronized void decreaseCut(double duration) {
        if(duration > lastDuration * THRESHOLD && lastDuration != 0) {
            decreaseSize(DELTA_SIZE);
        } else if(lastDuration > duration * THRESHOLD || (duration < Collections.max(Access.getDuration()))) {
            increaseSize(DELTA_SIZE);
        }
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
        left.release(BATCH_SIZE_MAP.get(name));
        curDuration = duration;
        decreaseCut(duration);
        lastDuration = duration;
        queue.sort();
    }

    public double getCurDuration() {
        return curDuration;
    }
}
