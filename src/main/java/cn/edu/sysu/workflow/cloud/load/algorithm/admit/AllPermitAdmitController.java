package cn.edu.sysu.workflow.cloud.load.algorithm.admit;

import cn.edu.sysu.workflow.cloud.load.balance.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.Arrays;
import java.util.List;

public class AllPermitAdmitController implements AdmitController {

    @Override
    public boolean[] admitControl(AdmitEnvironment admitEnvironment, List<ProcessInstance> processInstances) {
        boolean[] result = new boolean[processInstances.size()];
        Arrays.fill(result, true);
        return result;
    }

    @Override
    public String getName() {
        return "全允许";
    }
}
