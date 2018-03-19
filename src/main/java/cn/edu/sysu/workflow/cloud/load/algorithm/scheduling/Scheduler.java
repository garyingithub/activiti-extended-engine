package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.data.SimulatableProcessInstance;

import java.util.List;


public interface Scheduler {

    default void schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances) {
        processInstances.stream().
                forEach(processInstance -> {
                    environment.getPool()[0].generateWorkload((SimulatableProcessInstance) processInstance);
                });
    }

}
