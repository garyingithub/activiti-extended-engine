package cn.edu.sysu.workflow.cloud.load.approach;

import java.util.List;

public class RoundRobin implements Allocator {
    @Override
    public void allocate(Integer[][] serverCapacityArray, List<List<Integer>> instanceBuffer, List<Integer> result) {
        int i = 0;
        while (i < instanceBuffer.size()) {
            result.add(i % serverCapacityArray.length);
            i++;
        }
    }
}
