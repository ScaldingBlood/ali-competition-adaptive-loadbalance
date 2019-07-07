package com.aliware.tianchi.remote;

import com.aliware.tianchi.util.ScalableSemaphore;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Status {
    public static int batchSize = 30;
//    private static final Logger LOGGER = LoggerFactory.getLogger(Status.class);

    private int sum;
    private int maxNum;
    private ScalableSemaphore left;
    private volatile int cnt;

    private volatile double avgDuration = 0;

    private InvokerQueue queue;
    private String name;

    private static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static AtomicLong debugInfo;

    public Status(InvokerQueue queue, String name) {
        this.queue = queue;
        this.name = name;
    }

    public void init() {
        maxNum = (Access.maxAvailableThreads.get(name) - 10) / batchSize;
        this.sum = maxNum / 2;
        left = new ScalableSemaphore(this.sum * batchSize);
        debugInfo = new AtomicLong(this.sum * batchSize);
        cnt = this.sum;
    }

    public void increaseSize() {
        sum++;
        for(int i = 0; i < batchSize; i++) debugInfo.incrementAndGet();
        left.increasePermits(batchSize);
        cnt = sum + sum - 1;
    }

    public void decreaseSize() {
        sum--;
        for(int i = 0; i < batchSize; i++) debugInfo.decrementAndGet();
//        left.reducePermitsInternal(batchSize);
        cnt = sum + sum + 1;
    }

    public int getCnt() {
        return left.availablePermits();
    }

    public synchronized void decreaseCut(double duration) {
        cnt--;
        if(duration > avgDuration * 1.8 && sum > 1) {
            System.out.println(this.name + " decrease");
            decreaseSize();
            avgDuration = duration;
        } else {
            // release
            left.release(batchSize);
            for(int i = 0; i < batchSize; i++) debugInfo.incrementAndGet();

            avgDuration = avgDuration * 0.5 + duration * 0.5;
            if(cnt == 0 && sum < maxNum) {
                System.out.println(this.name + " increase");
                increaseSize();
            }
        }
        System.out.println("DURATION: " + name + " this: " + duration + " avg: " + avgDuration + " cnt: " + cnt + " sum: " + sum + " debug " + debugInfo.get());
//        LOGGER.info("DURATION: " + name + " this: " + duration + " avg: " + avgDuration + " cnt: " + cnt + " sum: " + sum);
    }

    public void acquire() {
        try {
            left.acquire();
            debugInfo.decrementAndGet();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release(double duration) {
        threadPool.submit( () -> {
            decreaseCut(duration);
            queue.sort();
        });
    }

    public double getAvgDuration() {
        return avgDuration;
    }
}
