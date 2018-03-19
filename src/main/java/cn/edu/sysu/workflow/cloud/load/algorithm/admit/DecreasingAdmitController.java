package cn.edu.sysu.workflow.cloud.load.algorithm.admit;

import cn.edu.sysu.workflow.cloud.load.balance.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.List;

public class DecreasingAdmitController implements AdmitController {

    @Override
    public boolean[] admitControl(AdmitEnvironment admitEnvironment,
                                  List<ProcessInstance> processInstances) {

        return new boolean[0];
    }
}
