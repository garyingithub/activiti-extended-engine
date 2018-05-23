package cn.edu.sysu.workflow.cloud.load.algorithm.admit;

import cn.edu.sysu.workflow.cloud.load.balance.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.List;

public class WeightedAdmitController implements AdmitController {

    @Override
    public boolean[] admitControl(AdmitEnvironment admitEnvironment, List<ProcessInstance> processInstances) {

        boolean[] result = new boolean[processInstances.size()];
        for(int i = 0; i < processInstances.size(); i++) {
            ProcessInstance processInstance = processInstances.get(i);
            admitEnvironment.assignProcessInstance(processInstance);
            if(!admitEnvironment.getServer().
                    checkOverload(processInstance.getFrequencyList())) {
                if(!admitEnvironment.checkWeightedOverload(processInstance)) {
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

    @Override
    public String getName() {
        return "TSEB";
    }
}
