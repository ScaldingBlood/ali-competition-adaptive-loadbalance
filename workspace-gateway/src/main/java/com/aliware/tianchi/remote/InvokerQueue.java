package com.aliware.tianchi.remote;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InvokerQueue {
    private String[] providers = new String[]{"medium", "large", "small"};

    private final double EPSILON = 0.10;

    private Map<String, Status> providerMap;

    private List<Map.Entry<String, Status>> entryList;

    private Lock lock = new ReentrantLock();

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

    public void sort() {
        if (lock.tryLock()) {
            entryList.sort((x, y) -> (int) (x.getValue().getCurDuration() - y.getValue().getCurDuration()));
            providers = entryList.stream().map(Map.Entry::getKey).toArray(String[]::new);
            lock.unlock();
        }
    }

    public String acquire() {
        String[] p = providers;
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < p.length; i++) {
            Status s = providerMap.get(p[i]);
            int count = s.getCnt();
            if (count > 10) {
                s.acquire();
                return p[i];
            }
            if (count == 0 || random.nextDouble() > EPSILON * count) {
                continue;
            }
            s.acquire();
            return p[i];
        }
        int pos = random.nextInt(p.length);
        providerMap.get(p[pos]).acquire();
        return p[pos];
    }
}