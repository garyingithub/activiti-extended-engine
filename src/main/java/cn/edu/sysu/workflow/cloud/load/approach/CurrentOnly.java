package cn.edu.sysu.workflow.cloud.load.approach;

import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CurrentOnly implements Allocator {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public void allocate(Integer[][] serverCapacityArray, List<ProcessInstance> buffer, List<Integer> result) {

        List<int[]> instanceBuffer = new ArrayList<>();
        for (ProcessInstance simulatableProcessInstance : buffer) {
            instanceBuffer.add(simulatableProcessInstance.getFrequencyList());
        }
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < instanceBuffer.size(); i++) {
            for (int j = 0; j < serverCapacityArray.length; j++) {
                boolean success = true;
                if (serverCapacityArray[j][0] > 0) {
                    success = false;
                }
                if (success) {
                    positions.add(j);
                    serverCapacityArray[j][0] -= instanceBuffer.get(i)[0];
                    break;
                }
            }
            if (positions.size() < i + 1) {
                positions.add(-1);
            }
        }
        result.addAll(positions);
        logger.info("max engine number id is " + result.stream().max(Comparator.naturalOrder()).get());
    }
}
