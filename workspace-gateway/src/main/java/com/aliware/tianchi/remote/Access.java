package com.aliware.tianchi.remote;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Access {
    public static Map<String, Status> providerMap;

    public static Map<String, Integer> maxAvailableThreads = new ConcurrentHashMap<>();

    public static InvokerQueue queue;

    public static List<Double> getDuration() {
        return providerMap.values().stream().map(Status::getCurDuration).collect(Collectors.toList());
    }
}
