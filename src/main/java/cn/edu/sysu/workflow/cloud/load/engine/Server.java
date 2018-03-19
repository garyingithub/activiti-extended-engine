package cn.edu.sysu.workflow.cloud.load.engine;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.TimeFollower;

import java.util.Arrays;

public class Server implements TimeFollower {
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
        int[] newArray = new int[remainingCapacity.length];
        System.arraycopy(remainingCapacity, 1, newArray, 0, remainingCapacity.length - 1);
        newArray[remainingCapacity.length - 1] = capacity;
        remainingCapacity = newArray;
    }
}
