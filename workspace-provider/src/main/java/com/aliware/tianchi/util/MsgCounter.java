package com.aliware.tianchi.util;


import java.util.concurrent.atomic.AtomicInteger;

public class MsgCounter {
    public static int BatchSize = 30;
    private volatile double[] duration = new double[BatchSize];
    private static AtomicInteger cnt  = new AtomicInteger();
    private String quota = System.getProperty("quota");

    public void callback() {
        double res = 0;
        for(double d : duration) res += d;
        String msg = quota + " " + res / BatchSize;
//        System.out.println(msg);
        Access.listener.receiveServerMsg(msg);
    }

    public void add(double duration) {
        int pos = cnt.getAndIncrement();
        if(pos == BatchSize) {
            callback();
            cnt.set(0);
        }
        this.duration[pos % BatchSize] = duration;
    }
}