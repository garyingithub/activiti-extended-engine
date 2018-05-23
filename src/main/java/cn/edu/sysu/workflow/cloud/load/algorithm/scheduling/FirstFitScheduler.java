package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FirstFitScheduler implements Scheduler {

    @Override
    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, List<AsynCallback> callbacks) {

        for(int i = 0; i < processInstances.size(); i++) {
            ProcessInstance instance = processInstances.get(i);

            System.out.println("start deploy");

            boolean ok = 1 == new StaticFirstFitScheduler().schedule(environment,
                    Collections.singletonList(processInstances.get(i)),
                    Collections.singletonList(callbacks.get(i)));

            while (!ok) {
                environment.addEngine();
                ok = 1 == new StaticFirstFitScheduler().schedule(environment,
                        Collections.singletonList(processInstances.get(i)),
                        Collections.singletonList(callbacks.get(i)));
            }
            System.out.println("end deploy");

        }

        return environment.getPool().size();

    }

    @Override
    public String getName() {
        return "BBFS";
    }
}
