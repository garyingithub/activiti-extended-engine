package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.List;


public interface Scheduler {

    default int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances) {
        processInstances.stream().
                forEach(processInstance -> {
                    environment.getPool().get(0).generateWorkload( processInstance);
                });
        return 1;
    }

    default int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, AsynCallback callback) {
        processInstances.stream().
                forEach(processInstance -> {
                    environment.getPool().get(0).generateWorkload( processInstance);
                    callback.call(processInstance, environment.getPool().get(0));
                });
        return 1;

    }




}
