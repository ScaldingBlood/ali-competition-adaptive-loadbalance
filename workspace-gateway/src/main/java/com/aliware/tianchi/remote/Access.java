package com.aliware.tianchi.remote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Access {
    public static Map<String, Status> providerMap;

    public static Map<String, Integer> maxAvailableThreads = new ConcurrentHashMap<>();
}
