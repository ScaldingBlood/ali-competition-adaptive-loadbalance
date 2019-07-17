package com.aliware.tianchi.remote;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InvokerQueue {
    private String[] providers = new String[] {"medium", "large", "small"};

    private Map<String, Status> providerMap;

    private List<Map.Entry<String, Status>> entryList;

    private Lock lock = new ReentrantLock();

    public InvokerQueue() {
        providerMap = new HashMap<>();
        for(int i = 0; i < providers.length; i++) {
            Status tmp = new Status(this, providers[i]);
            providerMap.put(providers[i], tmp);
        }
        for(Status s : providerMap.values())
            s.init();
        entryList = new ArrayList<>(providerMap.entrySet());
        Access.providerMap = providerMap;
    }

    public void sort() {
        if(lock.tryLock()) {
            entryList.sort(new Comparator<Map.Entry<String, Status>>() {
                @Override
                public int compare(Map.Entry<String, Status> x, Map.Entry<String, Status> y) {
                    return (int)(x.getValue().getCurDuration() - y.getValue().getCurDuration());
                }
            });
            for(int i=0;i<entryList.size();i++){
                providers[i]=entryList.get(i).getKey();
            }
            lock.unlock();
        }
    }

    public String acquire() {
        String[] p = providers;
        for(int i = 0; i < p.length; i++) {
            Status s = providerMap.get(p[i]);
            if(s.getAvailableCnt() > 0) {
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
