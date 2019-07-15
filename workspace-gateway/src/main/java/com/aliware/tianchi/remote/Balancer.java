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
    private static final int target = 1024 + DELTA_SIZE;
    private Map<String, Double> durations = new HashMap<>();

    public void balance(String p, double duration) {
        durations.put(p, duration);
        double maxD = 0, minD = Double.MAX_VALUE, sum = 0;
        for(double v : durations.values()) {
            maxD = v > maxD ? v : maxD;
            minD = v < minD ? v : minD;
            sum += v;
        }

        if (duration == minD && sum <= target) {
            Access.providerMap.get(p).increaseSize();
        }
        if (duration == maxD && sum > target) {
            Access.providerMap.get(p).decreaseSize();
        }
    }
}