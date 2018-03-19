package cn.edu.sysu.workflow.cloud.load.simulation;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.executor.LoadBalancer;
import cn.edu.sysu.workflow.cloud.load.executor.Main;
import cn.edu.sysu.workflow.cloud.load.executor.WorkflowEngine;
import cn.edu.sysu.workflow.cloud.load.executor.engine.SimulateActiviti;
import cn.edu.sysu.workflow.cloud.load.executor.log.LogExtractor;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.simulator.activiti.ActivitiSimuluator;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.SimulatableProcessInstance;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Simulator {

    int current;

    List<ProcessInstance> processInstances = new ArrayList<>();

    private List[] instances = new List[40000];
    LoadBalancer loadBalancer;

    int maxTimeSlot = 0;
    List<ProcessInstance> setTimeSlot(List<ProcessInstance> list) {

        List<ProcessInstance> processInstances = new ArrayList<>();
        processInstances.addAll(list);
        processInstances.sort(Comparator.comparingLong(o -> o.getTasks().get(0).getStart()));

        for(int i = 1; i < processInstances.size(); i++) {
            long gap = processInstances.get(i).getTasks().get(0).start -
                    processInstances.get(i - 1).getTasks().get(0).start;
            int timeSlot = Long.valueOf(gap / Constant.PERIOD).intValue();
            processInstances.get(i).setTimeSlot(timeSlot);
            if(instances[timeSlot] == null) {
                instances[timeSlot] = new ArrayList();
                maxTimeSlot++;
            }
            instances[timeSlot].add(processInstances.get(i));
        }
        return processInstances;
    }

    public Simulator() {
        File processDirectory = new File(Main.class.getClassLoader().getResource("processes").getPath());
        File[] processDefinitionFiles = processDirectory.listFiles();

        AtomicInteger count = new AtomicInteger(0);
        (Arrays.stream(processDefinitionFiles).parallel()).forEach(file -> {
//            loadBalancer.deployDefinition(file);

            BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
            InputStreamProvider provider = new ActivitiSimuluator.FileInputStreamProvider(file);
            BpmnModel model = bpmnXMLConverter.convertToBpmnModel(provider, false, false);

            String logFileName = file.getName().substring(0, file.getName().indexOf('.'));
            System.out.println(logFileName);
            File logFile = new File(Main.class.getClassLoader().getResource("logs/" + logFileName + ".mxml").getPath());

            List<ProcessInstance> instanceList = new LogExtractor().extractProcessInstance(logFile);

            instanceList.forEach(processInstance -> {
                SimulatableProcessInstance instance = (SimulatableProcessInstance) processInstance;
                instance.setTrace(new ActivitiUtil().buildTrace(model, processInstance));
            });
            this.processInstances.addAll(setTimeSlot(instanceList));
        });
        WorkflowEngine[] workflowEngines = new SimulateActiviti[Constant.PORTS.length];
        for(int i = 0; i < workflowEngines.length; i++) {
            workflowEngines[i] = new SimulateActiviti(Constant.ENGINE_CAPACITY, new HttpConfig());
        }
        loadBalancer = new LoadBalancer(workflowEngines);
    }

    public void simulate() {

        while (current < maxTimeSlot) {

            instances[current++].stream().forEach(new Consumer() {
                @Override
                public void accept(Object o) {
                    loadBalancer.launchProcessInstance((ProcessInstance) o);
                }
            });


            loadBalancer.pastPeriod();

        }
    }

    public static void main(String[] args) {
        new Simulator().simulate();
    }
}
