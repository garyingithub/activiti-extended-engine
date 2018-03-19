package cn.edu.sysu.workflow.cloud.load.approach;

import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;

import java.util.List;

public class BufferedFirstFit implements Allocator {
    @Override
    public void allocate(Integer[][] serverCapacityArray, List<ProcessInstance> buffer, List<Integer> result) {
//
//        List<ProcessInstance> instanceBuffer = new ArrayList<>();
//        for (ProcessInstance simulatableProcessInstance : buffer) {
//            instanceBuffer.add(simulatableProcessInstance);
//        }
//
//        instanceBuffer.sort((o1, o2) -> {
//            Integer o1Max = Arrays.stream(o1.getFrequencyList()).max(Integer::compareTo).get();
//            Integer o2Max = o2.getFrequencyList().stream().max(Integer::compareTo).get();
////            Double o1Value = (double) sumUpList(o1) / o1Max;
////            Double o2Value = (double) sumUpList(o2) / o2Max;
////            return o1Value.compareTo(o2Value);
//            return o1Max.compareTo(o2Max);
//        });
//        new FirstFit().allocate(serverCapacityArray, instanceBuffer, result);

    }

    private int sumUpList(List<Integer> list) {
        int result = 0;
        for (Integer element : list) {
            result += element;
        }
        return result;
    }


}
