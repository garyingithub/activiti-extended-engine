package cn.edu.sysu.workflow.cloud.load.executor;

import cn.edu.sysu.workflow.cloud.load.simulator.data.SimulatableProcessInstance;

import java.io.File;

public interface WorkflowEngine extends TimeFollower {
    void generateWorkload(SimulatableProcessInstance processInstance);
    void deployDefinition(File file);
}
