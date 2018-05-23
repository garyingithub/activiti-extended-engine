package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.BasicEngine;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;

public class StaticBestFitScheduler implements Scheduler {

    private double getMod(int[] values) {
        int result = Arrays.stream(values).reduce(0, (left, right) -> left + right * right);
        return Math.pow(result, 0.5);
    }
    double calculate(BasicEngine engine, ProcessInstance instance) {

//        int[] a = engine.getCapacityCopy();
//        double product = 0;
//
//        double instanceMod = getMod(instance.getFrequencyList());
//        for(int i = 0; i < a.length; i++) {
//            if(instance.getFrequencyList().length > i) {
//                product += a[i] * instance.getFrequencyList()[i];
//            } else {
//                product += 0 ;
//            }
//        }
//
//        ;
//        double result = product / (instanceMod * getMod(Arrays.copyOf(engine.getCapacityCopy(), instance.getFrequencyList().length)));
////        return result;
//
//
//        int maxPos = 0;
//        for(int i = 0; i < instance.getFrequencyList().length; i++) {
//            if(instance.getFrequencyList()[i] > instance.getFrequencyList()[maxPos]) {
//                maxPos = i;
//            }
//        }
//        if(engine.getCapacityCopy().length > maxPos) {
//            return engine.getCapacityCopy()[maxPos];
//
//        }
        return Arrays.stream(engine.getCapacityCopy()).min().getAsInt();
//        return a[0];
//        return result;
    }
    @Override
    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, List<AsynCallback> callbacks) {
        environment.getPool().sort((o2, o1) -> Double.compare(calculate(o1, processInstances.get(0)),
                calculate(o2, processInstances.get(0))));


        return new StaticFirstFitScheduler().schedule(environment, processInstances, Arrays.asList(callbacks.get(0)));
    }
}
