package com.aliware.tianchi.remote;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InvokerQueue {
    private String[] providers = new String[] {"medium", "large", "small"};

    private Map<String, Status> providerMap;

    public InvokerQueue() {
        providerMap = new HashMap<>();
        for(int i = 0; i < providers.length; i++) {
            Status tmp = new Status(providers[i]);
            providerMap.put(providers[i], tmp);
        }
        for(Status s : providerMap.values())
            s.init();
        Access.providerMap = providerMap;
    }

    public String acquire() {
        String[] p = providers;
        while(true) {
            int pos = ThreadLocalRandom.current().nextInt(p.length);
            Status s = providerMap.get(p[pos]);
            if(s.getAvailableCnt() > 0) {
                providerMap.get(p[pos]).acquire();
                return p[pos];
            }
        }
    }
}
