package cn.edu.sysu.workflow.cloud.load.executor.admit;

import cn.edu.sysu.workflow.cloud.load.executor.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;

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
