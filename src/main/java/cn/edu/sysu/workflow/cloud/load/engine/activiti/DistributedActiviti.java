package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import cn.edu.sysu.workflow.cloud.load.engine.ProcessEngine;
import cn.edu.sysu.workflow.cloud.load.simulator.data.TraceNode;
import org.activiti.bpmn.model.BpmnModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistributedActiviti implements ProcessEngine {

    private Map<String, BpmnModel> bpmnModelMap = new HashMap<>();
    private List<Activiti> activitiList = new ArrayList<>();

    @Override
    public String startProcess(String definitionId, Object data) {
        return null;
    }

    @Override
    public String claimTask(String processId, String taskName) {
        return null;
    }

    @Override
    public String deployProcessDefinition(String name, File file) {
        return null;
    }

    @Override
    public void executeTrace(String processId, TraceNode root) {

    }
}
