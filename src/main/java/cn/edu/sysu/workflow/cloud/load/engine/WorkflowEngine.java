package cn.edu.sysu.workflow.cloud.load.engine;

import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.io.File;

public interface WorkflowEngine {
    void generateWorkload(ProcessInstance processInstance);
    void deployDefinition(File file);
}
