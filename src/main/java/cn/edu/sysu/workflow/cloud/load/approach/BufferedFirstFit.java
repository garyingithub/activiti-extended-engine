package cn.edu.sysu.workflow.cloud.load.approach;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BufferedFirstFit implements Allocator {
    @Override
    public void allocate(Integer[][] serverCapacityArray, List<List<Integer>> instanceBuffer, List<Integer> result) {
        List<Integer> positions = new ArrayList<>();
        instanceBuffer.sort((o1, o2) -> {
            Integer o1Max = o1.stream().max(Integer::compareTo).get();
            Integer o2Max = o2.stream().max(Integer::compareTo).get();
            Double o1Value = (double) sumUpList(o1) / o1Max;
            Double o2Value = (double) sumUpList(o2) / o2Max;
            return o1Value.compareTo(o2Value);
        });
        for (int i = 0; i < instanceBuffer.size(); i++) {
            for (int j = 0; j < serverCapacityArray.length; j++) {
                boolean success = true;
                for (int k = 0; k < Math.min(instanceBuffer.get(i).size(), serverCapacityArray.length); k++) {
                    if (instanceBuffer.get(i).get(k) > serverCapacityArray[j][k]) {
                        success = false;
                        break;
                    }
                }
                if (success) {
                    positions.add(j);
                    for (int k = 0; k < Math.min(instanceBuffer.get(i).size(), serverCapacityArray.length); k++) {
                        serverCapacityArray[j][k] -= instanceBuffer.get(i).get(k);
                    }
                    break;
                }
            }
            if (positions.size() < i + 1) {
                positions.add(-1);
            }
        }
        result.addAll(positions);
        System.out.println("max engine number id is " + result.stream().max(Comparator.naturalOrder()).get());
    }

    private int sumUpList(List<Integer> list) {
        int result = 0;
        for (Integer element : list) {
            result += element;
        }
        return result;
    }


}
