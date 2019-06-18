package com.aliware.tianchi.remote;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InvokerQueue {
    private Map<Thread, BlockingQueue<String>> localQueue = new ConcurrentHashMap<>();
    private List<BlockingQueue<String>> queueList = new ArrayList<>();
    private int threadSize = Runtime.getRuntime().availableProcessors() * 2;
    private AtomicInteger cnt = new AtomicInteger();
    private volatile boolean flag = true;
    private Map<String, Integer> map = new ConcurrentHashMap<>();


    public InvokerQueue() {
    }

    public void initSize(String quota, int concurrentSize) {
        concurrentSize = concurrentSize - concurrentSize/10;
        map.put(quota, concurrentSize);
        cnt.incrementAndGet();
    }

    private BlockingQueue<String> initQueue() {
        int size = map.values().stream().reduce((a, b) -> a+b).get() / threadSize;
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(size);
        Map<String, Integer> record = new HashMap<>();
        for(String key : map.keySet()) {
//            map.put(key, size/map.get(key));
            record.put(key, ThreadLocalRandom.current().nextInt(map.size()));
        }
        for(int i = 0; i < size; i++) {
            Set<Map.Entry<String, Integer>> set = record.entrySet();
            Map.Entry<String, Integer> entry = set.stream().min(Comparator.comparingInt(Map.Entry::getValue)).get();
            record.put(entry.getKey(), entry.getValue() + size *  threadSize /map.get(entry.getKey()));
            queue.offer(entry.getKey());
        }
        return queue;
    }

    public String get() {
        Thread c = Thread.currentThread();
        if(flag) {
            if(cnt.get() == 3)
                flag = false;
            return null;
        }
        BlockingQueue<String> queue = localQueue.get(c);
        if(queue == null) {
            synchronized (this) {
                if(localQueue.get(c) == null) {
                    BlockingQueue<String> tmp = initQueue();
                    localQueue.put(c, tmp);
                    queueList.add(tmp);
                }
                queue = localQueue.get(c);
            }
        }
        try {
            return queue.poll(200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.out.println("Invoker fetch time out!");
            e.printStackTrace();
            return null;
        }
    }

    public void put(String s) {
        if(queueList.size() != 0) {
            queueList.get(ThreadLocalRandom.current().nextInt(queueList.size())).offer(s);
        }
    }
}
