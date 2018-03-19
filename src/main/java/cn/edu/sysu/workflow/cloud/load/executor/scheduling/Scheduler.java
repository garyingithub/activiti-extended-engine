package cn.edu.sysu.workflow.cloud.load.executor.scheduling;

import cn.edu.sysu.workflow.cloud.load.executor.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.SimulatableProcessInstance;

import java.util.List;


public interface Scheduler {

    default void schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances) {
        processInstances.stream().
                forEach(processInstance -> {
                    environment.getPool()[0].generateWorkload((SimulatableProcessInstance) processInstance);
                });
    }

}
