package com.aliware.tianchi.util;

import java.util.concurrent.Semaphore;

public class ScalableSemaphore extends Semaphore {
    public ScalableSemaphore(double permits) {
        super((int)permits);
    }

    public void reducePermitsInternal(double num) {
        super.reducePermits((int)num);
    }

    public void increasePermits(double num) {
        release((int)num);
    }
}
