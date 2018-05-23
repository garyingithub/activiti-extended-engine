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


            return Integer.compare(Arrays.stream(o1.getFrequencyList()).max().getAsInt(), Arrays.stream(o2.getFrequencyList()).max().getAsInt());
        }
    };

    Comparator<ProcessInstance> maxComparator = new Comparator<ProcessInstance>() {
        @Override
        public int compare(ProcessInstance o1, ProcessInstance o2) {
            return Integer.compare(Arrays.stream(o1.getFrequencyList()).max().getAsInt(), Arrays.stream(o2.getFrequencyList()).max().getAsInt());
        }
    };

    @Override
    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, List<AsynCallback> callbacks) {

//        processInstances.sort(normComparator);
        return new FirstFitScheduler().schedule(environment, processInstances, callbacks);


    }

    @Override
    public String getName() {
        return "BFDS";
    }
}
