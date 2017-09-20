package cn.edu.sysu.workflow.cloud.load.simulator.activiti;

import cn.edu.sysu.workflow.cloud.load.process.TraceNode;
import cn.edu.sysu.workflow.cloud.load.simulator.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.SimulatorUtil;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.process.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.process.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.simulator.Simulator;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;

import java.io.File;
import java.util.Map;

public class ActivitiSimuluator extends Simulator {

    private ActivitiUtil activitiUtil;
    private Map<String, TraceNode> traceNodeMap;
    private BpmnModel model;

    ActivitiSimuluator(File definitionFile, File logFile, HttpConfig httpConfig, ActivitiUtil activitiUtil) {
        super(logFile);
        this.activitiUtil = activitiUtil;
        this.setEngine(new Activiti(httpConfig));
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        InputStreamProvider provider = new FileInputStreamProvider(definitionFile);
        model = bpmnXMLConverter.convertToBpmnModel(provider, false, false);

        SimulatorUtil simulatorUtil = new SimulatorUtil();
        simulatorUtil.scanAndUploadDefinitions(new Activiti(httpConfig));
    }


    @Override
    public void simulate() {
        ProcessInstance processInstance = instanceMap.get(2);
        TraceNode root = activitiUtil.buildTrace(model, processInstance);
        final String instanceId = getEngine().startProcess(processInstance.getDefinitionId(), null);
        getEngine().executeTrace(instanceId, root);
//        executor.execute(() -> {
//            final String instanceId = getEngine().startProcess(processInstance.getDefinitionId(), null);
//            processInstance.setDefinitionId(instanceId);
//            getEngine().asyncStartTask(processInstance, 0, variableMap);
//        });
//        instanceMap.forEach((processInstance) -> executor.execute(() -> {
//            final String instanceId = getEngine().startProcess(processInstance.getDefinitionId(), null);
//            processInstance.setDefinitionId(instanceId);
//            getEngine().asyncStartTask(processInstance, 0, variableMap);
//        }));
    }
}
