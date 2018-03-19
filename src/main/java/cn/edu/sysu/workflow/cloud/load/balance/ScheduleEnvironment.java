package cn.edu.sysu.workflow.cloud.load.balance;

import cn.edu.sysu.workflow.cloud.load.TimeFollower;
import cn.edu.sysu.workflow.cloud.load.engine.WorkflowEngine;

import java.io.File;
import java.util.Arrays;

public class ScheduleEnvironment implements TimeFollower {
    private WorkflowEngine[] pool;

    public void deployDefinition(File file) {
        Arrays.stream(pool).parallel().forEach(workflowEngine -> workflowEngine.deployDefinition(file));
    }


    @Override
    public void pastPeriod() {
        Arrays.stream(pool).forEach(workflowEngine -> workflowEngine.pastPeriod());
    }

    public WorkflowEngine[] getPool() {
        return pool;
    }

    public ScheduleEnvironment(WorkflowEngine[] pool) {
        this.pool = pool;
    }
}
