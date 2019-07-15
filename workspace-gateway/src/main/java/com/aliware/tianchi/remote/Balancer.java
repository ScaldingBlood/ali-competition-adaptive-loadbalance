/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.aliware.tianchi.remote;

import static com.aliware.tianchi.remote.Status.DELTA_SIZE;

import java.util.*;

/**
 * @author yeling.cy
 * @version $Id: Balancer.java, v 0.1 2019年07月11日 10:07 yeling.cy Exp $
 */
public class Balancer {
    private static final int target = 1025;
    private Map<String, Double> durations = new HashMap<>();

    public void balance(String p, double duration) {
        durations.put(p, duration);
        double maxD = 0, minD = Double.MAX_VALUE;
        for(double v : durations.values()) {
            maxD = v > maxD ? v : maxD;
            minD = v < minD ? v : minD;
        }
        int sum = Access.providerMap.values().stream().map(Status::getSum).reduce(0, (x, y) -> x + y);

        if (duration == minD && sum <= target + DELTA_SIZE * 2) {
            Access.providerMap.get(p).increaseSize(DELTA_SIZE * 2);
        }
        if (duration == maxD && sum > target - DELTA_SIZE) {
            Access.providerMap.get(p).decreaseSize(DELTA_SIZE * 2);
        }
    }
}