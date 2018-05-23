package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.algorithm.HasName;
import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.List;


public interface Scheduler extends HasName {

    default int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances) {
        processInstances.stream().
                forEach(processInstance -> {
                    environment.getPool().get(0).generateWorkload( processInstance);
                });
        return 1;
    }

    default int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, List<AsynCallback> callbacks) {
//        processInstances.stream().
//                forEach(processInstance -> {
//                    environment.getPool().get(0).generateWorkload( processInstance);
//                    callback.call(processInstance, environment.getPool().get(0));
//                });
        for(int i = 0; i < processInstances.size(); i++) {
            ProcessInstance processInstance = processInstances.get(i);
            AsynCallback callback = callbacks.get(i);

            environment.getPool().get(0).generateWorkload( processInstance);
            callback.call(processInstance, environment.getPool().get(0));
        }
        return 1;

    }


    @Override
    default String getName() {
        return "基准算法";
    }




}
