package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.Arrays;
import java.util.List;

public class BestFitScheduler implements Scheduler {

    int calculate(int[] a, int[] b) {
        int result = 0;
        for(int i = 0; i < Math.min(a.length, b.length); i++) {
            result += (a[i] * b[i]);
        }
        return result;
    }

    @Override
    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, AsynCallback callback) {

        for(int i = 0; i < processInstances.size(); i++) {
            ProcessInstance instance = processInstances.get(i);

            boolean ok = 1 == new StaticBestFitScheduler().schedule(environment, Arrays.asList(processInstances.get(i)), callback);

            while (!ok) {
                environment.addEngine();
                ok = 1 == new StaticBestFitScheduler().schedule(environment, Arrays.asList(processInstances.get(i)), callback);
            }
        }

        return environment.getPool().size();

    }



}
