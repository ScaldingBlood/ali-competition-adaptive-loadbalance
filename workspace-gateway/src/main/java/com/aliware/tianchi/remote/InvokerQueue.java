package com.aliware.tianchi.remote;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InvokerQueue {
    private String[] providers = new String[] {"medium", "large", "small"};

    private Map<String, Status> providerMap;
    private List<Status> statuses;

    public void init() {
        providerMap = Access.providerMap;
        statuses = new ArrayList<>(providerMap.values());
    }

    public void sort() {
        List<Map.Entry<String, Status>> list = new ArrayList<>(providerMap.entrySet());
        list.sort((x, y) -> (int)(x.getValue().getAvgDuration() - y.getValue().getAvgDuration()));
        providers = list.stream().map(Map.Entry::getKey).toArray(String[]::new);
    }

    public String acquire() {
        String[] p = providers;
        for(int i = 0; i < p.length; i++) {
            Status s = statuses.get(i);
            if(s.getCnt() > 0) {
                s.acquire();
                return p[i];
            }
        }
        int index = ThreadLocalRandom.current().nextInt(p.length);
        statuses.get(index).getCnt();
        return providers[index];
    }
}
