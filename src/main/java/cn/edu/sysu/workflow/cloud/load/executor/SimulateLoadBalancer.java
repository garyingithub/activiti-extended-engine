package cn.edu.sysu.workflow.cloud.load.executor;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.executor.admit.AdmitController;
import cn.edu.sysu.workflow.cloud.load.executor.scheduling.Scheduler;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;

import java.util.ArrayList;
import java.util.List;

public class SimulateLoadBalancer implements TimeFollower {

    private Scheduler scheduler = new Scheduler() {};

    private AdmitController admitController = new AdmitController() {};

    private List<ProcessInstance> buffer = new ArrayList<>(Constant.BUFFER_SIZE);

    private AdmitEnvironment admitEnvironment;

    private ScheduleEnvironment scheduleEnvironment;


    public void launchProcessInstance(ProcessInstance instance) {
        buffer.add(instance);
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
            buffer.add(instance);
        }
    }

    @Override
    public void pastPeriod() {

    }
}
