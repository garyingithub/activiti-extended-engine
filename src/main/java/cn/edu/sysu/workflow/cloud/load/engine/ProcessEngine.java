package cn.edu.sysu.workflow.cloud.load.engine;

import cn.edu.sysu.workflow.cloud.load.engine.activiti.StringCallback;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.SimulatableProcessInstance;

import java.io.File;

public interface ProcessEngine {
    void startProcess(ProcessInstance processInstance, Object data, StringCallback callback);

    void claimTask(String processId, String taskName, StringCallback callback);

    void completeTask(String processId, String taskName, StringCallback callback);

    void deployProcessDefinition(String name, File file, StringCallback callback);

    void simulateProcessInstance(SimulatableProcessInstance processInstance);
}
