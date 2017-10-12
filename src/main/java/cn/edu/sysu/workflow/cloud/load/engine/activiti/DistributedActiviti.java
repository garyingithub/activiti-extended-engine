package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import cn.edu.sysu.workflow.cloud.load.engine.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.engine.ProcessEngine;
import cn.edu.sysu.workflow.cloud.load.engine.Server;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.TraceNode;
import org.activiti.bpmn.model.BpmnModel;

import java.io.File;
import java.util.*;

public class DistributedActiviti implements ProcessEngine {

    private Map<String, BpmnModel> bpmnModelMap = new HashMap<>();
    private List<Activiti> activitiList = new ArrayList<>();
    private Map<String, Activiti> activitiMap = new HashMap<>();

    private Random random = new Random();

    private Activiti pickActiviti(ProcessInstance processInstance) {
//        Activiti activiti = activitiList.get(random.nextInt(activitiList.size()));
        return max.chooseActiviti(processInstance, activitiList);
    }


    @FunctionalInterface
    interface LoadBalancer {
        Activiti chooseActiviti(ProcessInstance processInstance, List<Activiti> activitiList);
    }

    LoadBalancer randomBalancer = new LoadBalancer() {
        @Override
        public Activiti chooseActiviti(ProcessInstance processInstance, List<Activiti> activitiList) {
            return activitiList.get(random.nextInt(activitiList.size()));
        }
    };

    LoadBalancer vectorBasedBalancer = new LoadBalancer() {
        @Override
        public Activiti chooseActiviti(ProcessInstance processInstance, List<Activiti> activitiList) {
            Activiti activiti = activitiList.stream().min(Comparator.comparingDouble(o -> o.getSubtractedCapacityDeviation(processInstance.getFrequencyList()))).get();
            activiti.addLoad(processInstance.getFrequencyList());
            return activiti;
        }
    };

    LoadBalancer max = new LoadBalancer() {
        @Override
        public Activiti chooseActiviti(ProcessInstance processInstance, List<Activiti> activitiList) {
            return activitiList.stream().max(Comparator.comparingLong(Server::getSpace)).get();
        }
    };


    @Override
    public String startProcess(ProcessInstance processInstance, Object data) {
//        String definitionId = processInstance.getDefinitionId();
        Activiti activiti = pickActiviti(processInstance);
        String processId = activiti.startProcess(processInstance, data);
        activitiMap.put(processId, activiti);
        return processId;
    }

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

    public DistributedActiviti(List<HttpConfig> configs) {
        for (int i = 0; i < configs.size(); i++) {
            HttpConfig httpConfig = configs.get(i);
            activitiList.add(new Activiti(i, httpConfig));
        }
//        configs.forEach(httpConfig -> );
    }


}
