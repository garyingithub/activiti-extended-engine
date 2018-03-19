package cn.edu.sysu.workflow.cloud.load.approach;

import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;

import java.util.List;

@FunctionalInterface
public interface Allocator {
    void allocate(Integer[][] serverCapacityArray, List<ProcessInstance> instanceBuffer, List<Integer> result);

}
