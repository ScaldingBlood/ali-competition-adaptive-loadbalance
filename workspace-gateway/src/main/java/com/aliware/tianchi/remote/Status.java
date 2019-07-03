package com.aliware.tianchi.remote;

import com.aliware.tianchi.util.ScalableSemaphore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Status {
    public static int batchSize = 64;

    private int sum;
    private ScalableSemaphore left;
    private volatile int cnt;

    private volatile double avgDuration = 0;

    private InvokerQueue queue;

    private static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public Status(InvokerQueue queue) {
        this.queue = queue;
    }

    public void init(int sum) {
        this.sum = sum;
        left = new ScalableSemaphore(this.sum * batchSize);
        cnt = this.sum;
    }

    public void increaseSize() {
        sum++;
        left.increasePermits(batchSize);
        cnt = sum + sum - 1;
    }

    public void decreaseSize() {
        sum--;
        left.reducePermitsInternal(batchSize);
        cnt = sum + sum + 1;
    }

    public int getCnt() {
        return left.availablePermits();
    }

    public synchronized void decreaseCut(double duration) {
        cnt--;
        if(cnt == sum) {
            avgDuration = duration;
        } else if(cnt < sum) {
            avgDuration = avgDuration * 0.7 + duration * 0.3;
        } else {
            if(avgDuration * 1.5 <= duration) {
                decreaseSize();
            } else {
                increaseSize();
            }
        }
    }

    public void acquire() {
        try {
            left.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release(double duration) {
        threadPool.submit( () -> {
            left.release(batchSize);
            decreaseCut(duration);
            queue.sort();
        });
    }

    public double getAvgDuration() {
        return avgDuration;
    }
}
