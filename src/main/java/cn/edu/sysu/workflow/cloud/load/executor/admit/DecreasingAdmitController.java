package cn.edu.sysu.workflow.cloud.load.executor.admit;

import cn.edu.sysu.workflow.cloud.load.executor.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;

import java.util.List;

public class DecreasingAdmitController implements AdmitController {

    @Override
    public boolean[] admitControl(AdmitEnvironment admitEnvironment,
                                  List<ProcessInstance> processInstances) {

        return new boolean[0];
    }
}
