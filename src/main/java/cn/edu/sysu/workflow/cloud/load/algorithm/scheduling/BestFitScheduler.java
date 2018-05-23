package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.Activiti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class BestFitScheduler implements Scheduler {



//    @Override
//    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, AsynCallback callback) {
//
//        for(int i = 0; i < processInstances.size(); i++) {
//            ProcessInstance instance = processInstances.get(i);
//
//            boolean ok = 1 == new StaticBestFitScheduler().schedule(environment, Arrays.asList(processInstances.get(i)), callback);
//
//            while (!ok) {
//                environment.addEngine();
//                ok = 1 == new StaticBestFitScheduler().schedule(environment, Arrays.asList(processInstances.get(i)), callback);
//            }
//        }
//
//        return environment.getPool().size();
//
//    }

    @Override
    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, List<AsynCallback> callbacks) {


        for(int i = 0; i< processInstances.size();) {
            boolean deployed = false;
            for(int j = 0; j < environment.getPool().size(); j++) {
                if(!Constant.bigger(processInstances.get(i).getFrequencyList(), environment.getPool().get(j).getCapacityCopy())) {
                    environment.getPool().get(j).generateWorkload(processInstances.get(i));
                    callbacks.get(i).call((ProcessInstance) processInstances.get(i), environment.getPool().get(j));
                    deployed = true;
                    break;
                }
            }
            if (!deployed) {
                environment.addEngine();
            } else {
                i++;
            }
        }



        return environment.getPool().size();
    }

    @Override
    public String getName() {
        return "FFS";
    }
}
