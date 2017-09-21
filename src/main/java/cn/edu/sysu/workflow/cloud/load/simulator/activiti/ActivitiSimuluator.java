package cn.edu.sysu.workflow.cloud.load.simulator.activiti;

import cn.edu.sysu.workflow.cloud.load.engine.TraceNode;
import cn.edu.sysu.workflow.cloud.load.simulator.SimulatorUtil;
import cn.edu.sysu.workflow.cloud.load.engine.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.simulator.Simulator;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        for (int i = 0; i < 1000; i++) {
            instanceList.forEach(processInstance -> processInstance.getTasks().forEach(task -> task.setAvailable(true)));
            instanceList.forEach(processInstance -> {
                TraceNode root = activitiUtil.buildTrace(model, processInstance);
                final String instanceId = getEngine().startProcess(processInstance.getDefinitionId(), null);
                getEngine().executeTrace(instanceId, root);
            });
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
