package cn.edu.sysu.workflow.cloud.load.simulation;

import cn.edu.sysu.workflow.cloud.load.data.SimulatableProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;

public class SimulateActiviti extends Activiti {

    public SimulateActiviti(int capacity, HttpConfig httpConfig) {
        super(capacity, httpConfig);
    }

    @Override
    public void generateWorkload(SimulatableProcessInstance processInstance) {
        server.deployWorkload(processInstance.getFrequencyList());
    }
}
