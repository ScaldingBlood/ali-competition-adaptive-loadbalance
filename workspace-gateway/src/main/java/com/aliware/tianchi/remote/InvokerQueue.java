package com.aliware.tianchi.remote;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InvokerQueue {
    private String[] providers = new String[] {"medium", "large", "small"};

    private Map<String, Status> providerMap = new HashMap<>();
    private List<Status> statuses = new ArrayList<>();

    public InvokerQueue() {
        providerMap = new HashMap<>();
        for(int i = 0; i < providers.length; i++) {
            Status tmp = new Status(this, providers[i]);
            tmp.init();
            providerMap.put(providers[i], tmp);
            statuses.add(tmp);
        }
        Access.providerMap = providerMap;
    }

    public void sort() {
        List<Map.Entry<String, Status>> list = new ArrayList<>(providerMap.entrySet());
        list.sort((x, y) -> (int)(x.getValue().getCurDuration() - y.getValue().getCurDuration()));
        providers = list.stream().map(Map.Entry::getKey).toArray(String[]::new);
        System.out.println(Arrays.asList(providers));
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
        String target = p[ThreadLocalRandom.current().nextInt(p.length)];
        providerMap.get(target).acquire();
        return target;
    }
}
