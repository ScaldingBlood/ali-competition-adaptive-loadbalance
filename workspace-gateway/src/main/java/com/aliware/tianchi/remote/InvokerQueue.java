package com.aliware.tianchi.remote;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InvokerQueue {
    private String[] providers = new String[] {"medium", "large", "small"};

    private Map<String, Status> providerMap;

    public InvokerQueue() {
        providerMap = new HashMap<>();
        for(int i = 0; i < providers.length; i++) {
            Status tmp = new Status(this, providers[i]);
            tmp.init();
            providerMap.put(providers[i], tmp);
        }
        Access.providerMap = providerMap;
    }

    public void sort() {
        List<Map.Entry<String, Status>> list = new ArrayList<>(providerMap.entrySet());
        list.sort((x, y) -> (int)(x.getValue().getCurDuration() - y.getValue().getCurDuration()));
        providers = list.stream().map(Map.Entry::getKey).toArray(String[]::new);
    }

    public String acquire() {
        String[] p = providers;
        int[] queueLen = new int[p.length];
        for(int i = 0; i < p.length; i++) {
            Status s = providerMap.get(p[i]);
            int len = s.getQueueLen();
            if(len == 0) {
                s.acquire();
                return p[i];
            } else {
                queueLen[i] = len;
            }
        }
        int min = queueLen[0];
        int pos = 0;
        for(int i = 1; i < queueLen.length; i++)
            pos = queueLen[i] < min ? queueLen[i] : pos;
        providerMap.get(p[pos]).acquire();
        return p[pos];
    }
}
