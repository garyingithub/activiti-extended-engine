package process;

public interface ProcessEngine {
    <T> String startProcess(T processDefinition);

    String startTask(String processId, String taskName);

    String completeTask(String processId, String taskName);

     <T> String addProcessDefinition(String processDefinitionId, T paramObject) ;
}
