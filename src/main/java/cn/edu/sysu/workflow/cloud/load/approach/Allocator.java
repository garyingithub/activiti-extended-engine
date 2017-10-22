package cn.edu.sysu.workflow.cloud.load.approach;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@FunctionalInterface
public interface Allocator {
    void allocate(Integer[][] serverCapacityArray, List<List<Integer>> instanceBuffer, List<Integer> result);
}
