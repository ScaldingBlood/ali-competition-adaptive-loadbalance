package com.aliware.tianchi.remote;

import com.aliware.tianchi.CallbackListenerImpl;
import com.aliware.tianchi.util.ScalableSemaphore;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Status {
    public static int BATCH_SIZE = 40;
//    private static final Logger LOGGER = LoggerFactory.getLogger(Status.class);

    private int sum;
    private int maxNum;
    private ScalableSemaphore left;
    private volatile int cnt;

    private double lastDuration = 0;
    private double avgDuration = 0;
    private volatile double curDuration  = 0;

    private InvokerQueue queue;
    private String name;

    private static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public Status(InvokerQueue queue, String name) {
        this.queue = queue;
        this.name = name;
    }

    public void init() {
        maxNum = (Access.maxAvailableThreads.get(name)) / BATCH_SIZE;
        this.sum = maxNum / 2;
        left = new ScalableSemaphore(this.sum * BATCH_SIZE);
        cnt = this.sum;
    }

    public void increaseSize() {
        sum++;
        left.increasePermits(BATCH_SIZE);
        cnt = sum + sum - 1;
    }

    public void decreaseSize() {
        sum--;
//        left.reducePermitsInternal(BATCH_SIZE);
        cnt = sum + sum + 1;
    }

    public int getCnt() {
        return left.availablePermits();
    }

    public synchronized void decreaseCut(double duration) {
        cnt--;
        curDuration = duration;
        if(duration > avgDuration * 1.85 && sum > 1) {
            decreaseSize();
            lastDuration = duration;
            avgDuration = duration;
        } else {
            // release
            left.release(BATCH_SIZE);
            avgDuration = lastDuration;
            lastDuration = duration;
            if(cnt == 0 && sum < maxNum) {
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
        threadPool.submit( () -> {
            decreaseCut(duration);
            queue.sort();
        });
    }

    public double getCurDuration() {
        return curDuration;
    }
}
