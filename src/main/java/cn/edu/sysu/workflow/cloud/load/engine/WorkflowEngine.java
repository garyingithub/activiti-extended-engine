package cn.edu.sysu.workflow.cloud.load.engine;

import cn.edu.sysu.workflow.cloud.load.TimeFollower;
import cn.edu.sysu.workflow.cloud.load.data.SimulatableProcessInstance;

import java.io.File;

public interface WorkflowEngine extends TimeFollower {
    void generateWorkload(SimulatableProcessInstance processInstance);
    void deployDefinition(File file);
}
