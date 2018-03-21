package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.BasicEngine;

import java.util.Arrays;
import java.util.List;

public class StaticFirstFitScheduler implements Scheduler {

    @Override
    public int schedule(ScheduleEnvironment environment,
                        List<ProcessInstance> processInstances,
                        AsynCallback callback) {

        ProcessInstance instance = processInstances.get(0);
        for(BasicEngine basicEngine : environment.getPool()) {
            if(Arrays.stream(instance.getFrequencyList()).max().getAsInt() > Constant.ENGINE_CAPACITY) {
                for(int i = 0; i < instance.getFrequencyList().length; i++) {
                    if(instance.getFrequencyList()[i] > Constant.ENGINE_CAPACITY) {
                        instance.getFrequencyList()[i] = Constant.ENGINE_CAPACITY;
                    }
                }
            }
            if(!basicEngine.checkOverload(instance)) {
                basicEngine.generateWorkload(instance);
                callback.call(instance, basicEngine);
                return 1;
            }
        }

        return 0;
    }
}
