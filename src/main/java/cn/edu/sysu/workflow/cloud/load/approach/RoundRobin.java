package cn.edu.sysu.workflow.cloud.load.approach;

import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin implements Allocator {

    private static AtomicInteger i = new AtomicInteger(0);
    @Override
    public void allocate(Integer[][] serverCapacityArray, List<ProcessInstance> buffer, List<Integer> result) {
        List<int[]> instanceBuffer = new ArrayList<>();
        for (ProcessInstance simulatableProcessInstance : buffer) {
            instanceBuffer.add(simulatableProcessInstance.getFrequencyList());
        }
        result.add(i.getAndAdd(1) % serverCapacityArray.length);

    }


}
