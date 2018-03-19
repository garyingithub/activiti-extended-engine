package cn.edu.sysu.workflow.cloud.load.simulation;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.balance.LoadBalancer;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.data.SimulatableProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.WorkflowEngine;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.log.LogExtractor;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author mac
 */
public class Simulator {

    private int current;
    private List<List<ProcessInstance>> instances = new ArrayList<>();
    private LoadBalancer loadBalancer;


    private void setTimeSlot(List<ProcessInstance> processInstances) {
        processInstances.sort(Comparator.comparingLong(o -> o.getTasks().get(0).getStart()));

        for (int i = 1; i < processInstances.size(); i++) {
            long gap = processInstances.get(i).getTasks().get(0).start -
                    processInstances.get(i - 1).getTasks().get(0).start;
            int timeSlot = Long.valueOf(gap / Constant.PERIOD).intValue();
            processInstances.get(i).setTimeSlot(timeSlot);
            while (instances.size() <= timeSlot) {
                instances.add(new ArrayList<>());
            }
            instances.get(timeSlot).add(processInstances.get(i));
        }
    }

    public Simulator() {
        File processDirectory = Constant.getFileFromResource("processes");
        File[] processDefinitionFiles = processDirectory.listFiles();

        if (processDefinitionFiles == null) {
            throw new RuntimeException("No Process Definition");
        }
        (Arrays.stream(processDefinitionFiles).parallel()).forEach(file -> {

            BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
            InputStreamProvider provider = new Constant.FileInputStreamProvider(file);
            BpmnModel model = bpmnXMLConverter.convertToBpmnModel(provider, false, false);

            String logFileName = file.getName().substring(0, file.getName().indexOf('.'));

            File logFile = Constant.getFileFromResource("logs/" + logFileName + ".mxml");

            List<ProcessInstance> instanceList = LogExtractor.INSTANCE.extractProcessInstance(logFile);

            instanceList.forEach(processInstance -> {
                SimulatableProcessInstance instance = (SimulatableProcessInstance) processInstance;
                instance.setTrace(ActivitiUtil.INSTANCE.buildTrace(model, processInstance));
            });
            setTimeSlot(instanceList);
        });

        WorkflowEngine[] workflowEngines = new SimulateActiviti[Constant.PORTS.length];
        for (int i = 0; i < workflowEngines.length; i++) {
            workflowEngines[i] = new SimulateActiviti(Constant.ENGINE_CAPACITY, new HttpConfig());
        }
        loadBalancer = new LoadBalancer(workflowEngines, Constant.ENGINE_CAPACITY);
    }

    public void simulate() {

        while (current < (instances.size() + 500)) {
            if (instances.size() > current) {
                instances.get(current++).stream().
                        forEach(o -> loadBalancer.launchProcessInstance((ProcessInstance) o));
            }
            loadBalancer.pastPeriod();
        }
    }


    public static void main(String[] args) {
        new Simulator().simulate();
    }
}
