package cn.edu.sysu.workflow.cloud.load.algorithm.admit;

import cn.edu.sysu.workflow.cloud.load.balance.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.List;

public interface AdmitController {
    default boolean[] admitControl(AdmitEnvironment admitEnvironment,
                                   List<ProcessInstance> processInstances) {
        boolean[] result = new boolean[processInstances.size()];

        for(int i = 0; i < processInstances.size(); i++) {
            boolean ok = admitEnvironment.getServer().deployWorkload(processInstances.get(i).getFrequencyList());
            result[i] = ok;
        }
        return result;
    }
}
