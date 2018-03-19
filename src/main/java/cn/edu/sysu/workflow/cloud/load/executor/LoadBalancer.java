package cn.edu.sysu.workflow.cloud.load.executor;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.executor.admit.AdmitController;
import cn.edu.sysu.workflow.cloud.load.executor.admit.GreedyAdmitController;
import cn.edu.sysu.workflow.cloud.load.executor.scheduling.Scheduler;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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

    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    public LoadBalancer(WorkflowEngine[] engines) {
        this.scheduleEnvironment = new ScheduleEnvironment(engines);
        this.admitEnvironment = new AdmitEnvironment(Constant.ENGINE_CAPACITY);

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
