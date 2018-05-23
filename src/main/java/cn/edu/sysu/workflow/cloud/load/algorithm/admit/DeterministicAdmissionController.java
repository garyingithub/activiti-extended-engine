package cn.edu.sysu.workflow.cloud.load.algorithm.admit;

import cn.edu.sysu.workflow.cloud.load.balance.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.List;

public class DeterministicAdmissionController implements AdmitController {

    @Override
    public boolean[] admitControl(AdmitEnvironment admitEnvironment, List<ProcessInstance> processInstances) {
        return new boolean[0];
    }

    @Override
    public String getName() {
        return "精确算法";
    }
}
