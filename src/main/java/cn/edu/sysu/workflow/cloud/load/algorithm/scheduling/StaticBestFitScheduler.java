package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.Arrays;
import java.util.List;

public class StaticBestFitScheduler implements Scheduler {

    @Override
    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, AsynCallback callback) {
        environment.getPool().sort((o2, o1) -> Integer.compare(Arrays.stream(o2.getRemainingCapacity()).max().getAsInt(), Arrays.stream(o1.getRemainingCapacity()).max().getAsInt()));

        return new StaticFirstFitScheduler().schedule(environment, processInstances, callback);
    }
}
