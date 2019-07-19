package com.aliware.tianchi.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.dubbo.rpc.listener.CallbackListener;


public class Access {
    public static CallbackListener listener;
    public static MsgCounter msgCounter = new MsgCounter();
    public static BlockingQueue<Long> msgQueue = new ArrayBlockingQueue<>(1000);
}
