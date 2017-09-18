package cn.edu.sysu.workflow.cloud.load.simulator.activiti;

import cn.edu.sysu.workflow.cloud.load.SimulatorUtil;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.process.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.process.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.simulator.Simulator;
import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActivitiSimuluator extends Simulator {

    private ActivitiUtil activitiUtil;
    private Map<String, Map<String, Object>> variableMap;

    public ActivitiSimuluator(File definitionFile, File logFile, HttpConfig httpConfig, ActivitiUtil activitiUtil) {
        super(logFile);
        this.activitiUtil = activitiUtil;
        this.setEngine(new Activiti(httpConfig));
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        InputStreamProvider provider = new FileInputStreamProvider(definitionFile);
        BpmnModel model = bpmnXMLConverter.convertToBpmnModel(provider, false, false);
        variableMap = activitiUtil.parseForVariables(model);
        SimulatorUtil simulatorUtil = new SimulatorUtil();
        simulatorUtil.scanAndUploadDefinitions(new Activiti(httpConfig));
    }

    @Override
    public void simulate() {
        instanceMap.forEach((processInstance) -> executor.execute(() -> {
            final String instanceId = getEngine().startProcess(processInstance.getDefinitionId(), null);
            processInstance.getTasks().forEach(task -> {
                getEngine().startTask(instanceId, task.getTaskName());
                try {
                    TimeUnit.MILLISECONDS.sleep(task.getDuration());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                getEngine().completeTask(instanceId, task.getTaskName(), variableMap.get(task.getTaskName()));
            });
        }));
    }
}
