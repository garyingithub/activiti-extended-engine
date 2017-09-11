package cn.edu.sysu.workflow.cloud.load.process;

public interface ProcessEngine {
     String startProcess(String definitionId, Object data);

    String startTask(String processId, String taskName);

    String completeTask(String processId, String taskName);

    <ProcessDefinition> String addProcessDefinition(String name, String location);
}
