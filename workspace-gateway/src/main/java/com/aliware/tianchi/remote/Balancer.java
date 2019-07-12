/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.aliware.tianchi.remote;

import static com.aliware.tianchi.remote.Status.BATCH_SIZE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yeling.cy
 * @version $Id: Balancer.java, v 0.1 2019年07月11日 10:07 yeling.cy Exp $
 */
public class Balancer {
    private static final double target = 1025 / BATCH_SIZE;
    private Map<String, Double> durations = new HashMap<>();
    private Map<String, Integer> cnt = new HashMap<>();

    public void balance(String p, double duration) {
        durations.put(p, duration);
        cnt.put(p, cnt.getOrDefault(p, 1) - 1);
        int size = Access.providerMap.values().stream().map(Status::getSum).reduce(0, (x, y) -> x + y);

        if (duration == Collections.min(durations.values()) && size < target) {
            if (cnt.get(p) == 0) {
                Status tmp = Access.providerMap.get(p);
                tmp.increaseSize(1);
                tmp.increaseSize(1);
                cnt.put(p, tmp.getSum());
            }
        }
        if (duration == Collections.max(durations.values()) && size > target) {
            if (cnt.get(p) == 0) {
                Status tmp = Access.providerMap.get(p);
                tmp.decreaseSize(1);
                tmp.decreaseSize(1);
                cnt.put(p, tmp.getSum());
            }
        }
    }
}