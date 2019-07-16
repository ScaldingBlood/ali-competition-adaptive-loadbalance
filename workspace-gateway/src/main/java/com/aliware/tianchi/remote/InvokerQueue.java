package com.aliware.tianchi.remote;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InvokerQueue {
    private String[] providers = new String[]{"medium", "large", "small"};

    private String best = providers[0];

    private final double EPSILON = 0.0;

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
//        if (lock.tryLock()) {
//            entryList.sort((x, y) -> (int) (x.getValue().getCurDuration() - y.getValue().getCurDuration()));
//            providers = entryList.stream().map(Map.Entry::getKey).toArray(String[]::new);
//            lock.unlock();
//        }
        if (lock.tryLock()) {
            double fastest = providerMap.get(best).getCurDuration();
            for (String provider : providers) {
                double curDuration = providerMap.get(provider).getCurDuration();
                if (curDuration < fastest) {
                    best = provider;
                }
            }
            lock.unlock();
        }
    }

    public String acquire() {
        String[] p = providers;
        Random random = ThreadLocalRandom.current();
        Status s = providerMap.get(best);
        if (s.getCnt() > 0) {
            s.acquire();
            return best;
        }

        //get best of remaining with cnt > 0
        List<String> remainingProviders = Arrays.asList(p);
        remainingProviders.remove(best);

        double fastestDuration = providerMap.get(remainingProviders.get(0)).getCurDuration();
        String bestProvider = remainingProviders.get(0);

        for (String str : remainingProviders) {
            Status status = providerMap.get(str);
            if (status.getCnt() > 0 && status.getCurDuration() < fastestDuration) {
                bestProvider = str;
                fastestDuration = status.getCurDuration();
            }
        }
        best = bestProvider;
        return bestProvider;

//        String[] p = providers;
//        Random random = ThreadLocalRandom.current();
//        for (int i = 0; i < p.length; i++) {
//            Status s = providerMap.get(p[i]);
//            if (s.getCnt() > 0) {
//                if (random.nextDouble() < EPSILON) {
//                    continue;
//                }
//                s.acquire();
//                return p[i];
//            }
//        }
//        int pos = random.nextInt(p.length);
//        providerMap.get(p[pos]).acquire();
//        return p[pos];
    }
}