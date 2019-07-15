package com.aliware.tianchi.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MsgCounter {
    public static int BATCH_SIZE = 100;
    private String quota = System.getProperty("quota");
    private BlockingQueue<Double> durations;

    public MsgCounter() {
        durations = new ArrayBlockingQueue<>(1000);
//        double[] arr = new double[BATCH_SIZE];
        Thread callbackThread = new Thread(() -> {
            int cnt = 0;
            double avg = 0;
            while(true) {
                try {
//                    arr[cnt++] = durations.take();
                    avg += durations.take();
                    cnt++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(cnt == BATCH_SIZE) {
//                    double median = findK(arr, 0, BATCH_SIZE-1, BATCH_SIZE/2 + 1);
                    avg /= BATCH_SIZE;
                    String msg = quota + " " + avg;
                    //                   System.out.println(msg);
                    Access.listener.receiveServerMsg(msg);
                    cnt = 0;
                    avg = 0;
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

    public static double findK(double[] array, int left, int right, int k) {
        int i = partition(array, left, right);
        if (i == k - 1) {
            return array[k - 1];
        } else if (i > k - 1) {
            return findK(array, left, i - 1, k);
        } else {
            return findK(array, i + 1, right, k);
        }
    }

    public static int partition(double[] array, int left, int right) {
        double k = array[left];
        int i = left;
        int j = right;
        while (j > i) {
            while (array[j] < k && j > i) {
                j--;
            }
            if (j > i) {
                array[i] = array[j];
                i++;
            }
            while (array[i] > k && j > i) {
                i++;
            }
            if (j > i) {
                array[j] = array[i];
                j--;
            }
        }
        array[i] = k;
        return i;
    }
}