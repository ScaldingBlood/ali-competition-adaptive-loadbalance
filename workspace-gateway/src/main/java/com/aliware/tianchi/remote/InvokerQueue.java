package com.aliware.tianchi.remote;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InvokerQueue {
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

    private int target;

    public void judge() {
        new Thread(() -> {
            while(true) {
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
            }
        }).start();
    }


    public String acquire() {
        judge();
        list.get(target).acquire();
        return providers[target];
    }
}
