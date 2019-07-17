/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.aliware.tianchi.remote;

<<<<<<< HEAD
import org.apache.dubbo.common.utils.ConcurrentHashSet;

=======
import static com.aliware.tianchi.remote.Status.BATCH_SIZE;
>>>>>>> yeling/master
import static com.aliware.tianchi.remote.Status.DELTA_SIZE;

import java.util.*;

/**
 * @author yeling.cy
 * @version $Id: Balancer.java, v 0.1 2019年07月11日 10:07 yeling.cy Exp $
 */
public class Balancer {
    private static final double target = (1024 +  DELTA_SIZE) / BATCH_SIZE;
    private Map<String, Double> durations = new HashMap<>();
    private Set<String> set = new ConcurrentHashSet<>();

    public void balance(String p, double duration) {
        durations.put(p, duration);
<<<<<<< HEAD
        set.add(p);

        if(set.size() == 3) {
//        int sum = Access.providerMap.values().stream().map(Status::getSum).reduce(0, (x, y) -> x + y);
//        if(sum <= target) {
            enlarge();
//        }
//        if(sum > target) {
            restrict();
//        }
            set.clear();
            for(Map.Entry<String, Status> entry : Access.providerMap.entrySet()) {
                System.out.print(entry.getKey() + " " + entry.getValue().getSum());
            }
            System.out.println();
            Access.queue.sort();
        }
    }
=======
        double maxD = 0, minD = Double.MAX_VALUE, sum = 0;
        for(double v : durations.values()) {
            maxD = v > maxD ? v : maxD;
            minD = v < minD ? v : minD;
            sum += v;
        }
>>>>>>> yeling/master

        if (duration == minD && sum <= target) {
            Access.providerMap.get(p).increaseSize();
        }
        if (duration == maxD && sum > target) {
            Access.providerMap.get(p).decreaseSize();
        }
    }
}