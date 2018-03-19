package cn.edu.sysu.workflow.cloud.load.executor.admit;

import cn.edu.sysu.workflow.cloud.load.executor.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;

import java.util.List;

public class GreedyAdmitController implements AdmitController {
    @Override
    public boolean[] admitControl(AdmitEnvironment admitEnvironment, List<ProcessInstance> processInstances) {

        boolean[] result = new boolean[processInstances.size()];
        for(int i = 0; i < processInstances.size(); i++) {
            ProcessInstance processInstance = processInstances.get(i);
            admitEnvironment.assignProcessInstance(processInstance);
            if(!admitEnvironment.getServer().checkOverload(processInstance.getFrequencyList())) {
                if(!admitEnvironment.checkDominantOverload(processInstance)) {
                    admitEnvironment.admitProcessInstance(processInstance);
                    result[i] = true;
                }
            }
        }

        return result;
    }
}
