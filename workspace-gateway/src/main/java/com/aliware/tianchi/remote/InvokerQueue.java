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
        initWeightMap();
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
        double sum = 0;
        if (lock.tryLock()) {
            for (Map.Entry<String, Status> entry : entryList) {
                double curDuration = entry.getValue().getCurDuration();
                sum += 1.0 / (curDuration * curDuration * curDuration);
            }
            for (Map.Entry<String, Status> entry : entryList) {
                double curDuration = entry.getValue().getCurDuration();
                weightMap.put(entry.getKey(), (1.0 / (curDuration * curDuration * curDuration) / sum));
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
                weightMap.put(p[i], 0.0);
                refreshWeightMap();
                curDouble = nextDouble;
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

    private void refreshWeightMap() {
        Set<Map.Entry<String, Double>> entries = weightMap.entrySet();
        double sum = 0;
        for (Map.Entry<String, Double> entry : entries) {
            sum += entry.getValue();
        }
        if (sum == 0) {
            int index = random.nextInt(3);
            weightMap.put(providers[index], 1.0);
            return;
        }
        Map<String, Double> tempMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : entries) {
            tempMap.put(entry.getKey(), entry.getValue() / sum);
        }
        weightMap = tempMap;
    }

    private void initWeightMap() {
        double sum = 0;
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < providers.length; i++) {
            double randDouble = random.nextDouble();
            list.add(randDouble);
            sum += randDouble;
        }
        for (int i = 0; i < providers.length; i++) {
            double weightedDouble = list.get(i) / sum;
            weightMap.put(providers[i], weightedDouble);
        }
    }
}
