package cn.edu.sysu.workflow.cloud.load.simulator.activiti;

import cn.edu.sysu.workflow.cloud.load.engine.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.engine.ProcessEngine;
import cn.edu.sysu.workflow.cloud.load.simulator.SimulatorUtil;
import cn.edu.sysu.workflow.cloud.load.simulator.data.TraceNode;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.Simulator;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActivitiSimuluator extends Simulator {

    private BpmnModel model;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Map<ProcessInstance, TraceNode> traceNodeMap = new HashMap<>();

    public ActivitiSimuluator(File definitionFile, File logFile, ProcessEngine activiti, ActivitiUtil activitiUtil, SimulatorUtil simulatorUtil) {
        super(logFile, activiti);
        activiti.deployProcessDefinition(definitionFile.getName(), definitionFile);
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        InputStreamProvider provider = new FileInputStreamProvider(definitionFile);
        model = bpmnXMLConverter.convertToBpmnModel(provider, false, false);
        this.setSimulatorUtil(simulatorUtil);
        instanceList.forEach(processInstance -> {
            traceNodeMap.put(processInstance, activitiUtil.buildTrace(model, processInstance));
        });
    }

    @Override
    public void simulate() {
        instanceList.forEach(processInstance -> processInstance.getTasks().forEach(task -> task.setAvailable(true)));
        instanceList.sort(Comparator.comparingLong(o -> o.getTasks().get(0).getStart()));
        ProcessInstance lastInstance = null;


        for (ProcessInstance processInstance : instanceList) {
//            ProcessInstance processInstance = instanceList.get(2);
            if (lastInstance != null) {
                try {
                    TimeUnit.MILLISECONDS.sleep(processInstance.getTasks().get(0).getStart() - lastInstance.getTasks().get(0).getStart());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            final long start = System.currentTimeMillis();
            final String instanceId = getEngine().startProcess(processInstance, null);
//            logger.info("process of {} {} starts !", getLogFile().getName(), instanceId);
            getEngine().executeTrace(instanceId, traceNodeMap.get(processInstance));
            long end = System.currentTimeMillis();
//            logger.info("spend {} milliseconds", end - start);
            lastInstance = processInstance;
        }

        logger.info("finish starting the process instances");

    }

    class FileInputStreamProvider implements InputStreamProvider {

        private File file;

        FileInputStreamProvider(File file) {
            this.file = file;
        }

        @Override
        public InputStream getInputStream() {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
