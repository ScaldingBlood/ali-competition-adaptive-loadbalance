/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.aliware.tianchi.remote;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yeling.cy
 * @version $Id: Balancer.java, v 0.1 2019年07月11日 10:07 yeling.cy Exp $
 */
public class Balancer {
    private static final int target = 21;
    private Map<String, Double> durations = new HashMap<>();
    private Map<String, Integer> cnt = new HashMap<>();

    public void balance(String p, double duration) {
        durations.put(p, duration);
        cnt.put(p, cnt.getOrDefault(p, 1) - 1);
        int size = Access.providerMap.values().stream().map(Status::getSum).reduce(0, (x, y) -> x + y);

        if (duration == Collections.min(durations.values()) && size <= target) {
            if (cnt.get(p) == 0) {
                Status tmp = Access.providerMap.get(p);
                tmp.increaseSize();
                cnt.put(p, tmp.getSum());
            }
        }
        if (duration == Collections.max(durations.values()) && size > target) {
            if (cnt.get(p) == 0) {
                Status tmp = Access.providerMap.get(p);
                tmp.decreaseSize();
                cnt.put(p, tmp.getSum());
            }
        }
    }
}