package cn.edu.sysu.workflow.cloud.load.algorithm.admit;

import cn.edu.sysu.workflow.cloud.load.balance.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DecreasingWeightedAdmitController implements AdmitController {


    int calculateWeight(int[] workload) {
        int result = 0;

        for(int i = 0; i < workload.length; i++) {
            result += (1 + Double.valueOf(1.0d / (i + 1)).intValue()) * workload[i];
        }

        return result;
    }
    @Override
    public boolean[] admitControl(AdmitEnvironment admitEnvironment, List<ProcessInstance> processInstances) {




        processInstances.sort(new Comparator<ProcessInstance>() {
            @Override
            public int compare(ProcessInstance o2, ProcessInstance o1) {
                return Integer.compare(calculateWeight(o1.getFrequencyList()), calculateWeight(o2.getFrequencyList()));
            }
        });

        return new WeightedAdmitController().admitControl(admitEnvironment, processInstances);
    }

    @Override
    public String getName() {
        return "局部离线流程实例准入控制算法";
    }
}
