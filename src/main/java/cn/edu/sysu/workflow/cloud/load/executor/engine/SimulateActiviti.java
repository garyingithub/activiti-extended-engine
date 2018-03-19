package cn.edu.sysu.workflow.cloud.load.executor.engine;

import cn.edu.sysu.workflow.cloud.load.executor.Activiti;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.simulator.data.SimulatableProcessInstance;

public class SimulateActiviti extends Activiti {

    public SimulateActiviti(int capacity, HttpConfig httpConfig) {
        super(capacity, httpConfig);
    }

    @Override
    public void generateWorkload(SimulatableProcessInstance processInstance) {
        server.deployWorkload(processInstance.getFrequencyList());
    }
}
