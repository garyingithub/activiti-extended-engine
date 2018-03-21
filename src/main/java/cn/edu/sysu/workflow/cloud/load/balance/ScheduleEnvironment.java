package cn.edu.sysu.workflow.cloud.load.balance;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.TimeFollower;
import cn.edu.sysu.workflow.cloud.load.engine.BasicEngine;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.simulation.SimulateActiviti;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScheduleEnvironment implements TimeFollower {
    private List<BasicEngine> pool;

    public void deployDefinition(File file) {
        pool.stream().parallel().forEach(workflowEngine -> workflowEngine.deployDefinition(file));
    }


    @Override
    public void pastPeriod() {
        pool.stream().forEach(TimeFollower::pastPeriod);
    }

    public List<BasicEngine> getPool() {
        return pool;
    }

    public void addEngine() {
        this.pool.add(new SimulateActiviti(Constant.ENGINE_CAPACITY, new HttpConfig()));
    }
    public ScheduleEnvironment(BasicEngine[] pool) {
        this.pool = new ArrayList<>();
        for(BasicEngine engine : pool) {
            this.pool.add(engine);
        }
    }
}
