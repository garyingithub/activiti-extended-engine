package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.BasicEngine;

import java.util.Arrays;
import java.util.List;

public class DeterministicScheduler implements Scheduler {

    @Override
    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, List<AsynCallback> callbacks) {

        while (environment.getPool().size() < Constant.ENGINE_NUMBER) {
            environment.addEngine();
        }
        int[] plan = backTracking(environment, processInstances);

        for(int i = 0; i < plan.length; i++) {
            environment.getPool().get(plan[i]).generateWorkload(processInstances.get(i));
            callbacks.get(i).call(processInstances.get(i), environment.getPool().get(plan[i]));
        }
        return Arrays.stream(plan).max().getAsInt();
    }


    private int[] backTracking(ScheduleEnvironment environment, List<ProcessInstance> processInstances) {
        int[] result = new int[processInstances.size()];
        Arrays.fill(result, Integer.MAX_VALUE);
        int[] cur = new int[processInstances.size()];
        recursive(environment, processInstances, 0, result, cur);
        return result;
    }

    private void recursive(ScheduleEnvironment environment, List<ProcessInstance> processInstances, int i, int[] result, int[] cur) {
        int maxJ = Arrays.stream(result).max().getAsInt();

        if(i == processInstances.size()) {
            if(Arrays.stream(cur).max().getAsInt() < maxJ) {
                for(int j = 0; j < cur.length; j++) {
                    result[j] = cur[j];
                }
            }
            return;
        }

        for(int j = 0; j < environment.getPool().size() && j < maxJ; j++) {
            if(!environment.getPool().get(j).checkOverload(processInstances.get(i))) {
                cur[i] = j;
                environment.getPool().get(j).generateWorkload(processInstances.get(i));
                recursive(environment, processInstances, i + 1, result, cur);
                environment.getPool().get(j).deleteWorkload(processInstances.get(i));
            }
        }
    }

    @Override
    public String getName() {
        return "精确";
    }
}
