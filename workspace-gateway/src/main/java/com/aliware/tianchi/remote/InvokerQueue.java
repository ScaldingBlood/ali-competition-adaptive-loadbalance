package com.aliware.tianchi.remote;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InvokerQueue {
    private String[] providers = new String[] {"medium", "large", "small"};

    private Map<String, Status> providerMap;

    private List<Map.Entry<String, Status>> entryList;

    public InvokerQueue() {
        providerMap = new HashMap<>();
        for(int i = 0; i < providers.length; i++) {
            Status tmp = new Status(this, providers[i]);
            tmp.init();
            providerMap.put(providers[i], tmp);
        }
        entryList = new ArrayList<>(providerMap.entrySet());
        Access.providerMap = providerMap;
    }

    public void sort() {
        entryList.sort((x, y) -> (int)(x.getValue().getCurDuration() - y.getValue().getCurDuration()));
        providers = entryList.stream().map(Map.Entry::getKey).toArray(String[]::new);
    }

    public String acquire() {
        String[] p = providers;
        for(int i = 0; i < p.length; i++) {
            Status s = providerMap.get(p[i]);
            if(s.getCnt() > 0) {
                s.acquire();
                return p[i];
            }
        }
//        double min = ds[0];
//        int pos = 0;
//        for(int i = 1; i < ds.length; i++)
//            pos = ds[i] < min ? i : pos;
//        providerMap.get(p[pos]).acquire();
//        return p[pos];
        int pos = ThreadLocalRandom.current().nextInt(p.length);
        providerMap.get(p[pos]).acquire();
        return p[pos];
    }
}
