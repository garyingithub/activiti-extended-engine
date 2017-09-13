package cn.edu.sysu.workflow.cloud.load.process;

import java.util.Map;

public interface ProcessEngine {
     String startProcess(String definitionId, Object data);

    String startTask(String processId, String taskName);

    String completeTask(String processId, String taskName, Map<String, Object> variables);

    <ProcessDefinition> String addProcessDefinition(String name, String location);
}
