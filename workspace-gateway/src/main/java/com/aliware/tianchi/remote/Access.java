package com.aliware.tianchi.remote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Access {
    public static InvokerQueue queue;
    public static Map<String, Status> providerMap = new ConcurrentHashMap<>();

    public static volatile boolean isReady = false;
}
