package cn.edu.sysu.workflow.cloud.load.process;

import java.io.File;
import java.util.Map;

public interface ProcessEngine {
     String startProcess(String definitionId, Object data);

    String startTask(String processId, String taskName);

    String completeTask(String processId, String taskName, Map<String, Object> variables);

    String addProcessDefinition(String name, File file);

    void asyncCompleteTask(String processId, String taskName, Map<String, Object> variables);

    void executeTask(String processId, String taskName, long needTime, Map<String, Object> variables);
}
