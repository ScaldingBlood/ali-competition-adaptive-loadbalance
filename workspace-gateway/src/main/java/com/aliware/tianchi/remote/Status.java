package com.aliware.tianchi.remote;

import com.aliware.tianchi.util.ScalableSemaphore;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Status {
    public static int batchSize = 64;
    private static final Logger LOGGER = LoggerFactory.getLogger(Status.class);

    private int sum;
    private ScalableSemaphore left;
    private volatile int cnt;

    private volatile double avgDuration = 0;

    private InvokerQueue queue;
    private String name;

    private static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public Status(InvokerQueue queue, String name) {
        this.queue = queue;
        this.name = name;
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
        if(duration > avgDuration * 1.8) {
            decreaseSize();
            avgDuration = duration;
        } else {
            avgDuration = avgDuration * 0.5 + duration * 0.5;
            if(cnt == 0){
                increaseSize();
            }
        }
        System.out.println("DURATION: " + name + " this time: " + duration + " avg duration: " + avgDuration + " cnt: " + cnt + " sum: " + sum);
        LOGGER.info("DURATION: " + name + " this time: " + duration + " avg duration: " + avgDuration + " cnt: " + cnt + " sum: " + sum);
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
