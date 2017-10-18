package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import cn.edu.sysu.workflow.cloud.load.engine.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.engine.ProcessEngine;
import cn.edu.sysu.workflow.cloud.load.engine.Server;
import cn.edu.sysu.workflow.cloud.load.simulator.activiti.ActivitiSimuluator;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.TraceNode;
import org.activiti.bpmn.model.BpmnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
//import java.util.logging.Logger;

public class DistributedActiviti implements ProcessEngine {

    private Map<String, BpmnModel> bpmnModelMap = new HashMap<>();
    private List<Activiti> activitiList = new ArrayList<>();
    private Map<String, Activiti> activitiMap = new HashMap<>();

    private Random random = new Random();

    private Optional<Activiti> pickActiviti(ProcessInstance processInstance) {

        LoadBalancer balancer = bestFit;
        return balancer.chooseActiviti(processInstance, activitiList);
    }


    @FunctionalInterface
    interface LoadBalancer {
        Optional<Activiti> chooseActiviti(ProcessInstance processInstance, List<Activiti> activitiList);
    }

    LoadBalancer randomBalancer = new LoadBalancer() {
        @Override
        public Optional<Activiti> chooseActiviti(ProcessInstance processInstance, List<Activiti> activitiList) {
            return Optional.of(activitiList.get(random.nextInt(activitiList.size())));
        }
    };

    LoadBalancer vectorBasedBalancer = new LoadBalancer() {
        @Override
        public Optional<Activiti> chooseActiviti(ProcessInstance processInstance, List<Activiti> activitiList) {

            return activitiList.stream().
                    filter(activiti -> activiti.canAdd(processInstance.getFrequencyList())).
                    min(Comparator.comparingDouble(o -> o.getSubtractedCapacityDeviation(processInstance.getFrequencyList())));

        }
    };

    LoadBalancer bestFit = new LoadBalancer() {
        @Override
        public Optional<Activiti> chooseActiviti(ProcessInstance processInstance, List<Activiti> activitiList) {
            return activitiList.stream().
                    filter(activiti -> activiti.canAdd(processInstance.getFrequencyList())).
                    max(Comparator.comparingLong(Server::getSpace));

        }
    };

    List<StartProcessCallable> bufferList = new ArrayList<>();
    long start = 0;
    int maxBufferSize = 6;

    private Executor executor = Executors.newFixedThreadPool(3);

    private void dispatch() {
        if (bufferList.size() < maxBufferSize) return;
        Integer[][] serverCapacityArray = new Integer[activitiList.size()][activitiList.get(0).getCapacityArray().length];
        for (Integer i = 0; i < activitiList.size(); i++)
            serverCapacityArray[i] = activitiList.get(i).getCapacityArray();

        List<List<Integer>> instanceBuffer = new ArrayList<>();
        for (StartProcessCallable startProcessCallable : bufferList) {
            instanceBuffer.add(startProcessCallable.processInstance.getFrequencyList());
        }

        List<Integer> result = new ArrayList<>();
        dfs(new AtomicInteger(0), 0, serverCapacityArray, instanceBuffer, new ArrayList<>(), 0, result);
//        if (result.size() > 0) {
        for (int i = 0; i < bufferList.size(); i++) {
            if (result.get(i) == -1) continue;
            bufferList.get(i).setActiviti(activitiList.get(result.get(i)));
            bufferList.get(i).run();
        }
//        }

        bufferList.clear();
    }


    List<StartProcessCallable> callableBuffer = new ArrayList<>();

    @Override
    public String startProcess(ProcessInstance processInstance, Object data) {
        String definitionId = processInstance.getDefinitionId();
        Optional<Activiti> activitiOptional = pickActiviti(processInstance);
        if (activitiOptional.isPresent()) {
            String processId = activitiOptional.get().startProcess(processInstance, data);
            activitiOptional.get().addLoad(processInstance.getFrequencyList());
            activitiMap.put(processId, activitiOptional.get());
            ActivitiSimuluator.count.addAndGet(sumUpList(processInstance.getFrequencyList()));
            return processId;
        } else {
//            logger.info("fail to add load");
            return "";
        }


    }

    class StartProcessCallable implements Runnable {

        private ProcessInstance processInstance;
        private Object data;
        private Activiti activiti;
        private TraceNode root;
        private AtomicLong workloadCount;

        public void setActiviti(Activiti activiti) {
            this.activiti = activiti;
        }

        StartProcessCallable(ProcessInstance processInstance, Object data, TraceNode root, AtomicLong workloadCount) {
//            this.activiti = activiti;
            this.processInstance = processInstance;
            this.data = data;
            this.workloadCount = workloadCount;
            this.root = root;
        }

        @Override
        public void run() {
            String processId = activiti.startProcess(processInstance, data);
            activiti.addLoad(processInstance.getFrequencyList());
            activitiMap.put(processId, activiti);
            activiti.executeTrace(processId, root);
            for (Integer integer : processInstance.getFrequencyList()) {
                workloadCount.addAndGet(integer);
            }
//            return processId;
        }
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String claimTask(String processId, String taskName) {
        return activitiMap.get(processId).claimTask(processId, taskName);
    }

    @Override
    public String deployProcessDefinition(String name, File file) {
        activitiList.forEach(activiti -> activiti.deployProcessDefinition(name, file));
        return "";
    }

    @Override
    public void executeTrace(String processId, TraceNode root) {
        activitiMap.get(processId).executeTrace(processId, root);
    }

    @Override
    public synchronized String startProcessSimulation(ProcessInstance processInstance, Object data, TraceNode root, AtomicLong workloadCount) {
        bufferList.add(new StartProcessCallable(processInstance, data, root, workloadCount));
        dispatch();
        return "";
    }

    public DistributedActiviti(List<HttpConfig> configs) {
        for (int i = 0; i < configs.size(); i++) {
            HttpConfig httpConfig = configs.get(i);
            activitiList.add(new Activiti(i, httpConfig));
        }
//        configs.forEach(httpConfig -> );
    }


    public void dfs(AtomicInteger maxWorkload, int curWorkload, Integer[][] serverCapacityArray, List<List<Integer>> instanceBuffer, List<Integer> positions, int cur, List<Integer> result) {
        if (cur == instanceBuffer.size()) {
            if (maxWorkload.get() <= curWorkload) {
                result.clear();
                result.addAll(positions);
                maxWorkload.set(curWorkload);
            }
        } else {
            if (positions.size() <= cur) {
                positions.add(-1);
            }
            List<Integer> curInstance = instanceBuffer.get(cur);
            for (int i = 0; i < serverCapacityArray.length; i++) {
                boolean successful = true;
                for (int j = 0; j < curInstance.size(); j++) {
                    serverCapacityArray[i][j] -= curInstance.get(j);
                    curWorkload += curInstance.get(j);
                    if (serverCapacityArray[i][j] < 0) {
                        successful = false;
                    }
                }

                if (successful) {

                    positions.set(cur, i);
                    dfs(maxWorkload, curWorkload, serverCapacityArray, instanceBuffer, positions, cur + 1, result);
                }

                for (int j = 0; j < curInstance.size(); j++) {
                    serverCapacityArray[i][j] += curInstance.get(j);
                    curWorkload -= curInstance.get(j);
                }

            }
            positions.set(cur, -1);
            dfs(maxWorkload, curWorkload, serverCapacityArray, instanceBuffer, positions, cur + 1, result);
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
