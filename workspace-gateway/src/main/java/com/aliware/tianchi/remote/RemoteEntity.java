package com.aliware.tianchi.remote;


public class RemoteEntity {
    private String quota;
    private int activeCount;
    private int poolSize;

    public RemoteEntity(String quota, int poolSize) {
        this.quota = quota;
        this.poolSize = poolSize;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getQuota() {
        return quota;
    }
}
