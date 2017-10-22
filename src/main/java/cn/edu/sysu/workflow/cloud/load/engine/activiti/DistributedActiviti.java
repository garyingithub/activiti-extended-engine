package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import cn.edu.sysu.workflow.cloud.load.approach.BufferedFirstFit;
import cn.edu.sysu.workflow.cloud.load.approach.RoundRobin;
import cn.edu.sysu.workflow.cloud.load.engine.ProcessEngine;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.simulator.activiti.ActivitiSimuluator;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.SimulatableProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
//import java.util.logging.Logger;

public class DistributedActiviti implements ProcessEngine {

    private List<Activiti> activitiList = new ArrayList<>();
    private Map<String, Activiti> activitiMap = new HashMap<>();


//    AtomicInteger pos = new AtomicInteger(0);
//    LoadBalancer roundRobinBalancer = new LoadBalancer() {
//        @Override
//        public Optional<Activiti> chooseActiviti(ProcessInstance processInstance, List<Activiti> activitiList) {
//            return Optional.of(activitiList.get(pos.getAndAdd(1) % activitiList.size()));
//        }
//    };

    private int maxBufferSize = 20;

    private Executor executor = Executors.newFixedThreadPool(3);

    private void dispatch() {
        if (buffer.size() < maxBufferSize) {
            return;
        }
        Integer[][] serverCapacityArray = new Integer[activitiList.size()][activitiList.get(0).getCapacityArray().length];
        for (Integer i = 0; i < activitiList.size(); i++) {
            serverCapacityArray[i] = activitiList.get(i).getCapacityArray();
        }

        List<List<Integer>> instanceBuffer = new ArrayList<>();
        for (SimulatableProcessInstance simulatableProcessInstance : buffer) {
            instanceBuffer.add(simulatableProcessInstance.getFrequencyList());
        }

        List<Integer> result = new ArrayList<>();
        new BufferedFirstFit().allocate(serverCapacityArray, instanceBuffer, result);
//        roundRobin(new AtomicInteger(0), 0, serverCapacityArray, instanceBuffer, new ArrayList<>(), 0, result);

        for (int i = 0; i < buffer.size(); i++) {
            if (result.get(i) == -1) {
                continue;
            }
            final Activiti activiti = activitiList.get(result.get(i));
            final SimulatableProcessInstance simulatableProcessInstance = buffer.get(i);
            executor.execute(() -> activiti.simulateProcessInstance(simulatableProcessInstance));
        }

        buffer.clear();
    }

    private Optional<Activiti> pickActiviti(ProcessInstance processInstance) {
        return Optional.of(activitiList.get(0));
    }
    @Override
    public void startProcess(ProcessInstance processInstance, Object data, StringCallback callback) {
        Optional<Activiti> activitiOptional = pickActiviti(processInstance);
        if (activitiOptional.isPresent()) {
            activitiOptional.get().startProcess(processInstance, data, result -> {
                activitiMap.put(result, activitiOptional.get());
                callback.call(result);
            });
            activitiOptional.get().addLoad(processInstance.getFrequencyList());

            ActivitiSimuluator.count.addAndGet(sumUpList(processInstance.getFrequencyList()));
        }
    }


    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void claimTask(String processId, String taskName, StringCallback callback) {
        activitiMap.get(processId).claimTask(processId, taskName, callback);
    }

    @Override
    public void completeTask(String processId, String taskName, StringCallback callback) {
        activitiMap.get(processId).completeTask(processId, taskName, callback);
    }

    @Override
    public void deployProcessDefinition(String name, File file, StringCallback callback) {
        activitiList.forEach(activiti -> activiti.deployProcessDefinition(name, file, callback));
    }

    List<SimulatableProcessInstance> buffer = new ArrayList<>();
    @Override
    public synchronized void simulateProcessInstance(SimulatableProcessInstance processInstance) {
        buffer.add(processInstance);
        dispatch();
    }


    public DistributedActiviti(List<HttpConfig> configs) {
        for (int i = 0; i < configs.size(); i++) {
            HttpConfig httpConfig = configs.get(i);
            activitiList.add(new Activiti(i, httpConfig));
        }
    }


//    public void dfs(AtomicInteger maxWorkload, int curWorkload, Integer[][] serverCapacityArray, List<List<Integer>> instanceBuffer, List<Integer> positions, int cur, List<Integer> result) {
//        if (cur == instanceBuffer.size()) {
//            if (maxWorkload.get() <= curWorkload) {
//                result.clear();
//                result.addAll(positions);
//                maxWorkload.set(curWorkload);
//            }
//        } else {
//            if (positions.size() <= cur) {
//                positions.add(-1);
//            }
//            List<Integer> curInstance = instanceBuffer.get(cur);
//            for (int i = 0; i < serverCapacityArray.length; i++) {
//                boolean successful = true;
//                for (int j = 0; j < curInstance.size(); j++) {
//                    serverCapacityArray[i][j] -= curInstance.get(j);
//                    curWorkload += curInstance.get(j);
//                    if (serverCapacityArray[i][j] < 0) {
//                        successful = false;
//                    }
//                }
//
//                if (successful) {
//
//                    positions.set(cur, i);
//                    dfs(maxWorkload, curWorkload, serverCapacityArray, instanceBuffer, positions, cur + 1, result);
//                }
//
//                for (int j = 0; j < curInstance.size(); j++) {
//                    serverCapacityArray[i][j] += curInstance.get(j);
//                    curWorkload -= curInstance.get(j);
//                }
//
//            }
//            positions.set(cur, -1);
//            dfs(maxWorkload, curWorkload, serverCapacityArray, instanceBuffer, positions, cur + 1, result);
//        }
//
//    }
//
//
//    public void bufferedFirstFit(AtomicInteger maxWorkload, int curWorkload, Integer[][] serverCapacityArray, List<List<Integer>> instanceBuffer, List<Integer> positions, int cur, List<Integer> result) {
//        instanceBuffer.sort((o1, o2) -> {
//            Integer o1Max = o1.stream().max(Integer::compareTo).get();
//            Integer o2Max = o2.stream().max(Integer::compareTo).get();
//            Double o1Value = (double) sumUpList(o1) / o1Max;
//            Double o2Value = (double) sumUpList(o2) / o2Max;
//            return o1Value.compareTo(o2Value);
//        });
//        for(int i = 0; i < instanceBuffer.size(); i++) {
//            for(int j = 0; j <serverCapacityArray.length; j++) {
//                boolean success = true;
//                for(int k = 0; k < instanceBuffer.get(i).size(); k++) {
//                    if(instanceBuffer.get(i).get(k) > serverCapacityArray[j][k]) {
//                        success = false;
//                        break;
//                    }
//                }
//                if(success) {
//                    positions.add(j);
//                    for(int k = 0; k < instanceBuffer.get(i).size(); k++) {
//                        serverCapacityArray[j][k] -= instanceBuffer.get(i).get(k);
//                    }
//                    break;
//                }
//            }
//            if(positions.size() < i + 1) {
//                positions.add(-1);
//            }
//        }
//        result.addAll(positions);
//    }
//
//    public void firstFit(AtomicInteger maxWorkload, int curWorkload, Integer[][] serverCapacityArray, List<List<Integer>> instanceBuffer, List<Integer> positions, int cur, List<Integer> result) {
//
//    }
//
//    public void roundRobin(AtomicInteger maxWorkload, int curWorkload, Integer[][] serverCapacityArray, List<List<Integer>> instanceBuffer, List<Integer> positions, int cur, List<Integer> result) {
//        int i = 0;
//        while (i < instanceBuffer.size()) {
//            result.add(i % serverCapacityArray.length);
//            i++;
//        }
//    }

    private int sumUpList(List<Integer> list) {
        int result = 0;
        for (Integer element : list) {
            result += element;
        }
        return result;
    }


}
