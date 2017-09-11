package cn.edu.sysu.workflow.cloud.load.simulator;

import cn.edu.sysu.workflow.cloud.load.SimulatorUtil;
import cn.edu.sysu.workflow.cloud.load.process.ProcessEngine;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Simulator {
    private ProcessEngine engine;
    private File logFile;

    private Map<Integer, ProcessInstance> instanceMap;

    private Executor executor = Executors.newCachedThreadPool();

    public Simulator(File file) {
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(file);
            Element root = document.getRootElement();
            Element process = root.getChild("Process");

            List<ProcessInstance> instances = (List<ProcessInstance>) process.getChildren("ProcessInstance").stream().map((Object object) -> {
                ProcessInstance instance = new ProcessInstance();
                Element element = (Element)object;
                List<ProcessInstance.Task> tasks = new ArrayList<>();
                for(Object taskObject : element.getChildren("AuditTrailEntry")) {
                    Element taskElement = (Element)taskObject;
                    String taskName = taskElement.getChildText("WorkflowModelElement");
                    String taskEvent = taskElement.getChildText("EventType");
                    String timeStampString = taskElement.getChildText("Timestamp");
                    String originator = taskElement.getChildText("Originator");

                    long timeStamp = SimulatorUtil.parseTimeStampString(timeStampString);
                    if(!taskName.contains("EVENT") && !taskName.equals("subProcess") && !(originator!=null && originator.equals("Automated Service"))) {
                        if(taskEvent.equals("assign")) {
                            ProcessInstance.Task task = new ProcessInstance.Task();
                            task.setTaskName(taskName);
                            task.setDuration(timeStamp);
                            tasks.add(task);
                        } else {
                            for(int i = tasks.size() - 1; i >= 0; i--) {
                                if(tasks.get(i).getTaskName().equals(taskName)) {
                                    tasks.get(i).setDuration(timeStamp - tasks.get(i).getDuration());
                                }
                            }
                        }
                    }
                }
                instance.setTasks(tasks);
                instance.setDefinitionId("hireProcessWithJpa");
                return instance;
            }).collect(Collectors.toList());
        } catch (JDOMException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void simulate() {
        instanceMap.forEach((key, processInstance) -> executor.execute(() -> {
            Map<String, String> data = new HashMap<>();
            data.put("name", "gary");
            data.put("email", "s");
            data.put("phoneNumber", "111");
            final String instanceId = engine.startProcess(processInstance.getDefinitionId(), data);
            processInstance.getTasks().forEach(task -> {
                engine.startTask(instanceId, task.getTaskName());
                try {
                    TimeUnit.MILLISECONDS.sleep(task.getDuration());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }));
    }

    public ProcessEngine getEngine() {
        return engine;
    }

    public void setEngine(ProcessEngine engine) {
        this.engine = engine;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    public Map<Integer, ProcessInstance> getInstanceMap() {
        return instanceMap;
    }

    public void setInstanceMap(Map<Integer, ProcessInstance> instanceMap) {
        this.instanceMap = instanceMap;
    }
}
