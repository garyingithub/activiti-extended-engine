package cn.edu.sysu.workflow.cloud.load.engine;

import cn.edu.sysu.workflow.cloud.load.Constant;

import java.util.Arrays;

public class Server implements ResourceOwner {
    private long id;
    public long getId() {
        return id;
    }
    private int capacity;
    private int[] remainingCapacity;

    public Server(int capacity) {
        this.capacity = capacity;
        remainingCapacity = new int[Constant.WATCHED_PERIOD_NUMBER];
        Arrays.fill(remainingCapacity, capacity);
        id = Constant.SERVER_ID_GENERATOR.getAndAdd(1);
    }

    @Override
    public synchronized boolean deployWorkload(final int[] workload) {
        if(checkOverload(workload)) {
            return false;
        }
        for(int i = 0; i < workload.length; i++) {
            if(i < remainingCapacity.length) {
                remainingCapacity[i] -= workload[i];
            }
        }
        return true;
    }


    @Override
    public synchronized boolean checkOverload(int[] workload) {
        for(int i = 0; i < workload.length; i++) {
            if(i < remainingCapacity.length && remainingCapacity[i] < workload[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void pastPeriod() {
        Constant.pastPeriod(this.remainingCapacity, this.capacity);
    }

    public int[] getRemainingCapacity() {
        return Arrays.copyOf(remainingCapacity, remainingCapacity.length);
    }
}
