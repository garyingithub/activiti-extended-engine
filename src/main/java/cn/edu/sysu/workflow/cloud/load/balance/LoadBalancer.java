package cn.edu.sysu.workflow.cloud.load.balance;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.TimeFollower;
import cn.edu.sysu.workflow.cloud.load.algorithm.admit.AdmitController;
import cn.edu.sysu.workflow.cloud.load.algorithm.admit.GreedyAdmitController;
import cn.edu.sysu.workflow.cloud.load.algorithm.scheduling.Scheduler;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.WorkflowEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LoadBalancer implements TimeFollower {

    private Scheduler scheduler = new Scheduler() {};

    private AdmitController admitController = new GreedyAdmitController();

    private List<ProcessInstance> buffer = new ArrayList<>(Constant.BUFFER_SIZE);

    private AdmitEnvironment admitEnvironment;

    private ScheduleEnvironment scheduleEnvironment;

    public synchronized void launchProcessInstance(ProcessInstance processInstance) {

        buffer.add(processInstance);
        if(buffer.size() == Constant.BUFFER_SIZE) {
            boolean[] admits = admitController.admitControl(admitEnvironment, buffer);

            List<ProcessInstance> admittedInstances = new ArrayList<>();
            for(int i = 0; i < buffer.size(); i++) {
                if(admits[i]) {
                    admittedInstances.add(buffer.get(i));
                }
            }
            scheduler.schedule(scheduleEnvironment, admittedInstances);
            buffer.clear();
        } else {
            buffer.add(processInstance);
        }
    }

    public LoadBalancer(WorkflowEngine[] engines, int capacity) {
        this.scheduleEnvironment = new ScheduleEnvironment(engines);
        this.admitEnvironment = new AdmitEnvironment(capacity);

    }

    public void deployDefinition(File file) {
        this.scheduleEnvironment.deployDefinition(file);
    }
    @Override
    public void pastPeriod() {
        scheduleEnvironment.pastPeriod();
        admitEnvironment.pastPeriod();
    }
}
