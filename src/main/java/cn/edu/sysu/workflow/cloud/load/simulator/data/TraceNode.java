package cn.edu.sysu.workflow.cloud.load.simulator.data;

import java.util.List;
import java.util.Map;

public class TraceNode {

    private List<TraceNode> nextNodes;
    private Task task;
    private Map<String, Object> variables;

    public List<TraceNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<TraceNode> nextNodes) {
        this.nextNodes = nextNodes;
    }


    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
