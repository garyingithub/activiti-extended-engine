package cn.edu.sysu.workflow.cloud.load.algorithm.admit;

import cn.edu.sysu.workflow.cloud.load.balance.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GreedyAdmitController implements AdmitController {
    @Override
    public boolean[] admitControl(AdmitEnvironment admitEnvironment, List<ProcessInstance> processInstances) {

        processInstances.sort(new Comparator<ProcessInstance>() {
            @Override
            public int compare(ProcessInstance o1, ProcessInstance o2) {
                return Integer.compare(Arrays.stream(o2.getFrequencyList()).max().getAsInt(), Arrays.stream(o1.getFrequencyList()).max().getAsInt());
            }
        });
        boolean[] result = new boolean[processInstances.size()];
        for(int i = 0; i < processInstances.size(); i++) {
            ProcessInstance processInstance = processInstances.get(i);
            admitEnvironment.assignProcessInstance(processInstance);
            if(!admitEnvironment.getServer().checkOverload(processInstance.getFrequencyList())) {
                if(!admitEnvironment.checkDominantOverload(processInstance)) {
                    admitEnvironment.admitProcessInstance(processInstance);

                    result[i] = true;
                } else {
                    result[i] = false;
                }
            } else {
                result[i] = false;
            }
        }

        return result;
    }
}
