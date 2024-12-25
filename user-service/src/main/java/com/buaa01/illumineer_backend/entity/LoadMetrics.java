package com.buaa01.illumineer_backend.entity;

public class LoadMetrics {
    private final double systemLoad;
    private final int activeThreads;
    private final long usedMemory;
    private final double loadIndex;

    public LoadMetrics(double systemLoad, int activeThreads, long usedMemory, double loadIndex) {
        this.systemLoad = systemLoad;
        this.activeThreads = activeThreads;
        this.usedMemory = usedMemory;
        this.loadIndex = loadIndex;
    }

    public double getSystemLoad() {
        return systemLoad;
    }

    public int getActiveThreads() {
        return activeThreads;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public double getLoadIndex() {
        return loadIndex;
    }
}