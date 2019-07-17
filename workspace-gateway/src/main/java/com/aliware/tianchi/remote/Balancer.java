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
//    private static final int target = 1024;
    private Map<String, Double> durations = new HashMap<>();
    private Set<String> set = new HashSet<>();

    public void balance(String p, double duration) {
        durations.put(p, duration);
        set.add(p);

        if(set.size() == 3) {
//        int sum = Access.providerMap.values().stream().map(Status::getSum).reduce(0, (x, y) -> x + y);
//        if(sum <= target) {
            enlarge();
//        }
//        if(sum > target) {
            restrict();
//        }
            set = new HashSet<>();
        }
        for(Map.Entry<String, Status> entry : Access.providerMap.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue().getSum());
        }
        System.out.println();
    }

    public void enlarge() {
        List<Map.Entry<String, Double>> list = new ArrayList<>(durations.entrySet());
        list.sort((x, y) -> (int)(x.getValue() - y.getValue()));
        for(Map.Entry<String, Double> entry : list) {
            if (Access.providerMap.get(entry.getKey()).increaseSize(DELTA_SIZE)) {
                return;
            }
        }
    }

    public void restrict() {
        List<Map.Entry<String, Double>> list = new ArrayList<>(durations.entrySet());
        list.sort((x, y) -> (int)(y.getValue() - x.getValue()));
        for(Map.Entry<String, Double> entry : list) {
            if (Access.providerMap.get(entry.getKey()).decreaseSize(DELTA_SIZE)) {
                return;
            }
        }
    }
}
