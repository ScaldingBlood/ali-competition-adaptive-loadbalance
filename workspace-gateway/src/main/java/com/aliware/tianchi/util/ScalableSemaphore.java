package com.aliware.tianchi.util;

import java.util.concurrent.Semaphore;

public class ScalableSemaphore extends Semaphore {
    public ScalableSemaphore(int permits) {
        super(permits);
    }

    public void reducePermitsInternal(int num) {
        super.reducePermits(num);
    }

    public void increasePermits(int num) {
        release(num);
    }
}
