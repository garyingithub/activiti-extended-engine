package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FFDScheduler implements Scheduler {

    Comparator<ProcessInstance> normComparator = new Comparator<ProcessInstance>() {
        @Override
        public int compare(ProcessInstance o2, ProcessInstance o1) {
            return Integer.compare(o1.getFrequencyList()[0], o2.getFrequencyList()[0]);
        }
    };

    Comparator<ProcessInstance> maxComparator = new Comparator<ProcessInstance>() {
        @Override
        public int compare(ProcessInstance o2, ProcessInstance o1) {
            return Integer.compare(Arrays.stream(o1.getFrequencyList()).max().getAsInt(), Arrays.stream(o2.getFrequencyList()).max().getAsInt());
        }
    };

    @Override
    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, AsynCallback callback) {

//        processInstances.sort(Comparator.comparingInt(o1 -> Arrays.stream(o1.getFrequencyList()).max().getAsInt()));

//        processInstances.sort(new Comparator<ProcessInstance>() {
//            @Override
//            public int compare(ProcessInstance o1, ProcessInstance o2) {
//
//                double[] share0 = new double[o1.getFrequencyList().length];
//                double[] share1 = new double[o2.getFrequencyList().length];
//
//                for(int i = 0; i < share0.length; i++) {
//
//                    for(int j = 0; j < environment.getPool().size(); j++) {
//                        if(environment.getPool().get(j).getRemainingCapacity().length > i) {
//                            share0[i] += (double) o1.getFrequencyList()[i] / environment.getPool().get(j).getRemainingCapacity()[i];
//                        }
//                    }
//                }
//
//                for(int i = 0; i < share1.length; i++) {
//
//                    for(int j = 0; j < environment.getPool().size(); j++) {
//                        if(environment.getPool().get(j).getRemainingCapacity().length > i) {
//                            share1[i] += (double) o2.getFrequencyList()[i] / environment.getPool().get(j).getRemainingCapacity()[i];
//                        }
//                    }
//                }
//
//                return Double.compare(Arrays.stream(share1).max().getAsDouble(), Arrays.stream(share0).max().getAsDouble());
//            }
//        });

        System.out.println("start sort the process instance");
        processInstances.sort(normComparator);
        return new FirstFitScheduler().schedule(environment, processInstances, callback);


    }
}
