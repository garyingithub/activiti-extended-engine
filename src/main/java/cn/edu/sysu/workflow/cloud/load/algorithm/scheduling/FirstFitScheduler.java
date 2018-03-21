package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.Arrays;
import java.util.List;

public class FirstFitScheduler implements Scheduler {

    @Override
    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, AsynCallback callback) {

        for(int i = 0; i < processInstances.size(); i++) {
            ProcessInstance instance = processInstances.get(i);

            System.out.println("start deploy");

            boolean ok = 1 == new StaticFirstFitScheduler().schedule(environment, Arrays.asList(processInstances.get(i)), callback);

            while (!ok) {
                environment.addEngine();
                ok = 1 == new StaticFirstFitScheduler().schedule(environment, Arrays.asList(processInstances.get(i)), callback);
            }
            System.out.println("end deploy");

        }

        return environment.getPool().size();

    }
}
