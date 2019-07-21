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
    private Map<String, Double> durations = new HashMap<>();
    private Set<String> set = new HashSet<>();

    public void balance(String p, double duration) {
        if(Access.providerMap.get(p).getState().compareTo(StateEnum.LIMIT) != 0)
            return;
        durations.put(p, duration);
        set.add(p);
        System.out.println("balance " + p);

        if(set.size() == 3) {
            enlarge();
            restrict();
            set.clear();
            for(Map.Entry<String, Status> entry : Access.providerMap.entrySet()) {
                System.out.print(entry.getKey() + " " + entry.getValue().getUpperBound());
            }
            System.out.println();
        }
    }

    public void enlarge() {
        List<Map.Entry<String, Double>> list = new ArrayList<>(durations.entrySet());
        list.sort((x, y) -> (int)(x.getValue() - y.getValue()));
        for(Map.Entry<String, Double> entry : list) {
            if (Access.providerMap.get(entry.getKey()).increaseSize()) {
                return;
            }
        }
    }

    public void restrict() {
        List<Map.Entry<String, Double>> list = new ArrayList<>(durations.entrySet());
        list.sort((x, y) -> (int)(y.getValue() - x.getValue()));
        for(Map.Entry<String, Double> entry : list) {
            if (Access.providerMap.get(entry.getKey()).decreaseSize()) {
                return;
            }
        }
    }
}
