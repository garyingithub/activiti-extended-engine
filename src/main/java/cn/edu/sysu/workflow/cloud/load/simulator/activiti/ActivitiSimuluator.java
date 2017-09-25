package cn.edu.sysu.workflow.cloud.load.simulator.activiti;

import cn.edu.sysu.workflow.cloud.load.engine.TraceNode;
import cn.edu.sysu.workflow.cloud.load.simulator.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.SimulatorUtil;
import cn.edu.sysu.workflow.cloud.load.engine.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.simulator.Simulator;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActivitiSimuluator extends Simulator {

    private ActivitiUtil activitiUtil;
    private Map<String, TraceNode> traceNodeMap;
    private BpmnModel model;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ActivitiSimuluator(File definitionFile, File logFile, HttpConfig httpConfig, ActivitiUtil activitiUtil, SimulatorUtil simulatorUtil) {
        super(logFile);
        this.activitiUtil = activitiUtil;
        Activiti activiti = new Activiti(httpConfig);
        this.setEngine(activiti);
        activiti.deployProcessDefinition(definitionFile.getName(), definitionFile);
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        InputStreamProvider provider = new FileInputStreamProvider(definitionFile);
        model = bpmnXMLConverter.convertToBpmnModel(provider, false, false);
    }

    @Override
    public void simulate() {
//        for (int i = 0; i < 1; i++) {
        instanceList.forEach(processInstance -> processInstance.getTasks().forEach(task -> task.setAvailable(true)));
//            ProcessInstance processInstance = instanceList.get(2);
        instanceList.sort(Comparator.comparingLong(o -> o.getTasks().get(0).getStart()));
        ProcessInstance lastInstance = null;


        for (ProcessInstance processInstance : instanceList) {
            if (lastInstance != null) {
                try {
                    TimeUnit.MILLISECONDS.sleep(processInstance.getTasks().get(0).getStart() - lastInstance.getTasks().get(0).getStart());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            TraceNode root = activitiUtil.buildTrace(model, processInstance);
            final long start = System.currentTimeMillis();
            final String instanceId = getEngine().startProcess(processInstance.getDefinitionId(), null);
            logger.info("process of {} {} starts !", getLogFile().getName(), instanceId);
            getEngine().executeTrace(instanceId, root);
            long end = System.currentTimeMillis();
            logger.info("spend {} milliseconds", end - start);
            lastInstance = processInstance;
        }

        logger.info("finish starting the process instances");

    }

}
