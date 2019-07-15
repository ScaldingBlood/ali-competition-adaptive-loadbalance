package com.aliware.tianchi.remote;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InvokerQueue {
    private String[] providers = new String[]{"medium", "large", "small"};

    private Map<String, Status> providerMap;

    private List<Map.Entry<String, Status>> entryList;

    private Lock lock = new ReentrantLock();

    private Map<String, Double> weightMap = new HashMap<>();

    private Random random = new Random();

    public InvokerQueue() {
        providerMap = new HashMap<>();
        for (int i = 0; i < providers.length; i++) {
            Status tmp = new Status(this, providers[i]);
            tmp.init();
            providerMap.put(providers[i], tmp);
        }
        entryList = new ArrayList<>(providerMap.entrySet());
        Access.providerMap = providerMap;
    }

//    public void sort() {
//        if (lock.tryLock()) {
//            entryList.sort((x, y) -> (int) (x.getValue().getCurDuration() - y.getValue().getCurDuration()));
//            providers = entryList.stream().map(Map.Entry::getKey).toArray(String[]::new);
//            lock.unlock();
//        }
//    }

    public void sort() {
        providers = entryList.stream().map(Map.Entry::getKey).toArray(String[]::new);
        int sum = 0;
        if (lock.tryLock()) {
            for (Map.Entry<String, Status> entry : entryList) {
                sum += 1.0 / entry.getValue().getCurDuration();
            }
            for (Map.Entry<String, Status> entry : entryList) {
                double curDuration = entry.getValue().getCurDuration();
                weightMap.put(entry.getKey(), (1.0 / curDuration) / sum);
            }
            lock.unlock();
        }
    }


    public String acquire() {
        double nextDouble = random.nextDouble();
        double curDouble = nextDouble;
        String[] p = providers;
        for (int i = 0; i < p.length; i++) {
            if (curDouble > weightMap.get(p[i])) {
                curDouble -= weightMap.get(p[i]);
                continue;
            }
            Status s = providerMap.get(p[i]);
            if (s.getCnt() > 0) {
                s.acquire();
                return p[i];
            } else {
                curDouble = nextDouble * (1 - weightMap.get(p[i]));
                weightMap.put(p[i], 0.0);
                i = 0;
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
