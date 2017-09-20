package cn.edu.sysu.workflow.cloud.load.process;

import cn.edu.sysu.workflow.cloud.load.simulator.ProcessInstance;

import java.io.File;
import java.util.Map;

public interface ProcessEngine {
    String startProcess(String definitionId, Object data);

    String startTask(String processId, String taskName);


    String addProcessDefinition(String name, File file);

    void executeTrace(String processId, TraceNode root);

}
