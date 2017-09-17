package cn.edu.sysu.workflow.cloud.load.process;

import java.io.File;
import java.util.Map;

public interface ProcessEngine {
     String startProcess(String definitionId, Object data);

    String startTask(String processId, String taskName);

    String completeTask(String processId, String taskName, Map<String, Object> variables);

    String addProcessDefinition(String name, File file);
}
