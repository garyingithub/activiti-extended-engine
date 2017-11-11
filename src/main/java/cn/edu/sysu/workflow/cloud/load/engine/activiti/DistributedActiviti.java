package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import cn.edu.sysu.workflow.cloud.load.approach.BufferedFirstFit;
import cn.edu.sysu.workflow.cloud.load.approach.FirstFit;
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


    private int sumUpList(List<Integer> list) {
        int result = 0;
        for (Integer element : list) {
            result += element;
        }
        return result;
    }


}
