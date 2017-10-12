package cn.edu.sysu.workflow.cloud.load.engine;

import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.TraceNode;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

public interface ProcessEngine {
    String startProcess(ProcessInstance processInstance, Object data);

    String claimTask(String processId, String taskName);

    String deployProcessDefinition(String name, File file);

    void executeTrace(String processId, TraceNode root);

    String startProcessSimulation(ProcessInstance processInstance, Object data, TraceNode root, AtomicLong workloadCount);
}
