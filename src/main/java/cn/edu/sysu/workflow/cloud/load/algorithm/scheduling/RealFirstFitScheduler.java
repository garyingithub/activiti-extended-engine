package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.List;

public class RealFirstFitScheduler implements Scheduler {

    @Override
    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, AsynCallback callback) {

        for(int i = 0; i < processInstances.size(); i++) {
            ProcessInstance instance = processInstances.get(i);
            boolean ok = internal(environment, instance, callback);;
            while (!ok) {
                environment.addEngine();
                ok = internal(environment, instance, callback);
            }
        }
        return environment.getPool().size();
    }


    private boolean internal(ScheduleEnvironment environment, ProcessInstance instance, AsynCallback callback) {
        for(int j = 0; j < environment.getPool().size(); j++) {
            if(environment.getPool().get(j).getRemainingCapacity()[0] > Math.floorDiv(Constant.ENGINE_CAPACITY * 1, 10)) {
                environment.getPool().get(j).generateWorkload(instance);
                callback.call(instance, environment.getPool().get(j));
                return true;
            }

        }
        return false;
    }
}
