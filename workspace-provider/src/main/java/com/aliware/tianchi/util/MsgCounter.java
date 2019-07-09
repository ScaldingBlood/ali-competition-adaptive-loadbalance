package com.aliware.tianchi.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MsgCounter {
    public static int BatchSize = 30;
    private String quota = System.getProperty("quota");
    private BlockingQueue<Double> durations;

    public MsgCounter() {
        durations = new ArrayBlockingQueue<>(1000);
        Thread callbackThread = new Thread(() -> {
            int cnt = 0;
            double res = 0;
            while(true) {
                try {
                    res += durations.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cnt++;
               if(cnt == BatchSize) {
                   String msg = quota + " " + res / BatchSize;
//                   System.out.println(msg);
                   Access.listener.receiveServerMsg(msg);
                   res = 0;
                   cnt = 0;
               }
            }
        });
        callbackThread.start();
    }


    public void add(double duration) {
        try {
            durations.put(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}