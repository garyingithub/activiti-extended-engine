package cn.edu.sysu.workflow.cloud.load.algorithm.admit;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.algorithm.HasName;
import cn.edu.sysu.workflow.cloud.load.balance.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.List;

public interface AdmitController extends HasName {
    default boolean[] admitControl(AdmitEnvironment admitEnvironment,
                                   List<ProcessInstance> processInstances) {
        boolean[] result = new boolean[processInstances.size()];

        for(int i = 0; i < processInstances.size(); i++) {
            ProcessInstance processInstance = processInstances.get(i);
            admitEnvironment.assignProcessInstance(processInstance);
            if(admitEnvironment.getServer().getCapacityCopy()[0] < Double.valueOf(Constant.ENGINE_CAPACITY * Constant.ENGINE_CAPACITY * 0.9).intValue()) {
                result[i] = true;
                admitEnvironment.admitProcessInstance(processInstance);

            } else {
                result[i] = false;
            }
        }
        return result;
    }

    @Override
    default String getName() {
        return "RLB";
    }
}
