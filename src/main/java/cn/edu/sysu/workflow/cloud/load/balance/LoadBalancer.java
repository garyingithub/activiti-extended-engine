package cn.edu.sysu.workflow.cloud.load.balance;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.TimeFollower;
import cn.edu.sysu.workflow.cloud.load.algorithm.admit.AdmitController;
import cn.edu.sysu.workflow.cloud.load.algorithm.scheduling.FFDScheduler;
import cn.edu.sysu.workflow.cloud.load.algorithm.scheduling.Scheduler;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.BasicEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LoadBalancer implements TimeFollower {

    private Scheduler scheduler = new FFDScheduler();

    private AdmitController admitController = new AdmitController() {};

    private List<ProcessInstance> buffer = new ArrayList<>(Constant.BUFFER_SIZE);
    private List<AsynCallback> callbackBuffer = new ArrayList<>(Constant.BUFFER_SIZE);


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

    public synchronized int launchProcessInstance(ProcessInstance processInstance, AsynCallback callback) {

        buffer.add(processInstance);
        callbackBuffer.add(callback);
        if( buffer.size() == Constant.BUFFER_SIZE) {
            boolean[] admits = admitController.admitControl(admitEnvironment, buffer);

            List<ProcessInstance> admittedInstances = new ArrayList<>();
            for(int i = 0; i < buffer.size(); i++) {
                if(admits[i]) {
                    admittedInstances.add(buffer.get(i));
                }
            }

            System.out.println("start schedule");
            int result = scheduler.schedule(scheduleEnvironment, admittedInstances, callbackBuffer);
            System.out.println("end schedule");

            buffer.clear();
            callbackBuffer.clear();
            return result;
        }
        return 0;
    }

    public LoadBalancer(BasicEngine[] engines, int capacity) {
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

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setAdmitController(AdmitController admitController) {
        this.admitController = admitController;
    }
}
