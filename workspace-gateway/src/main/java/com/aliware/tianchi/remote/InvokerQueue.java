package com.aliware.tianchi.remote;

import java.lang.annotation.Target;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InvokerQueue {
    private static final int ACQUIRE_SIZE = 4;

    private List<Status> list = new ArrayList<>();
    private String[] providers = new String[] {"medium", "large", "small"};

//    private Lock lock = new ReentrantLock();

    public InvokerQueue() {
        Map<String, Status> providerMap = new HashMap<>();
        for(int i = 0; i < providers.length; i++) {
            Status tmp = new Status(providers[i]);
            providerMap.put(providers[i], tmp);
            list.add(tmp);
        }
        for(Status s : providerMap.values())
            s.init();
        Access.providerMap = providerMap;
        judge();
    }

//    public void sort() {
//        if(lock.tryLock()) {
//            List<Map.Entry<String, Status>> entryList = new ArrayList<>(providerMap.entrySet());
//            entryList.sort((x, y) -> (int) (x.getValue().getCurDuration() - y.getValue().getCurDuration()));
//            for(int i = 0; i < entryList.size(); i++)
//                providers[i] = entryList.get(i).getKey();
//            lock.unlock();
//        }
//    }
    private AtomicInteger cnt = new AtomicInteger();
    private volatile int target = 0;
    public int judge() {
        if((cnt.getAndIncrement() & (ACQUIRE_SIZE-1)) != 0) {
            return target;
        }
        Status targetStatus = list.get(0);
        StateEnum targetState = targetStatus.getState();
        double targetDuration = targetStatus.getCurDuration();
        for (int i = 1; i < 3; i++) {
            Status s = list.get(i);
            StateEnum tmpState = s.getState();
            double tmpDuration = s.getCurDuration();
            if (tmpState.compareTo(targetState) < 0) {
                target = i;
                targetState = tmpState;
                targetDuration = s.getCurDuration();
            } else if (tmpState.compareTo(targetState) == 0) {
                if (tmpState.compareTo(StateEnum.LIMIT) == 0) {
                    target = targetStatus.getLeft() > s.getLeft() ? target : i;
                } else if (tmpDuration < targetDuration) {
                    target = i;
                    targetDuration = tmpDuration;
                }
            }
        }
        list.get(target).acquire(ACQUIRE_SIZE);
        return target;
    }


    public String acquire() {
        int target = judge();
        return providers[target];
    }
}
