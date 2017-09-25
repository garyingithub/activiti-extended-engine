package cn.edu.sysu.workflow.cloud.load.simulator;

import cn.edu.sysu.workflow.cloud.load.engine.ProcessEngine;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toList;


public abstract class Simulator {
    private ProcessEngine engine;
    private File logFile;

    protected List<ProcessInstance> instanceList;

    protected Executor executor = Executors.newCachedThreadPool();


    private SimulatorUtil simulatorUtil = new SimulatorUtil();

    public List<ProcessInstance> getInstanceList() {
        return instanceList;
    }

    public void setInstanceList(List<ProcessInstance> instanceList) {
        this.instanceList = instanceList;
    }

    protected Simulator() {

    }
    public Simulator(File file) {
        this.logFile = file;
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(file);
            Element root = document.getRootElement();
            Element process = root.getChild("Process");

            this.instanceList = (List<ProcessInstance>) process.getChildren("ProcessInstance").stream().map((Object object) -> {
                ProcessInstance instance = new ProcessInstance();
                Element element = (Element)object;
                List<ProcessInstance.Task> tasks = new ArrayList<>();
                for(Object taskObject : element.getChildren("AuditTrailEntry")) {
                    Element taskElement = (Element)taskObject;
                    String taskName = taskElement.getChildText("WorkflowModelElement");
                    String taskEvent = taskElement.getChildText("EventType");
                    String timeStampString = taskElement.getChildText("Timestamp");
                    String originator = taskElement.getChildText("Originator");

                    long timeStamp = simulatorUtil.parseTimeStampString(timeStampString);
                    if(!taskName.contains("EVENT") && !taskName.equals("subProcess") && !(originator!=null && originator.equals("Automated Service"))) {
                        if(taskEvent.equals("assign")) {
                            ProcessInstance.Task task = new ProcessInstance.Task();
                            task.setTaskName(taskName);
                            task.setStart(timeStamp);
                            tasks.add(task);
                        } else {
                            for(int i = tasks.size() - 1; i >= 0; i--) {
                                if(tasks.get(i).getTaskName().equals(taskName)) {
                                    tasks.get(i).setEnd(timeStamp);
                                }
                            }
                        }
                    }
                }
                instance.setTasks(tasks);
                instance.setDefinitionId(file.getName().substring(0, file.getName().indexOf('.')));
                return instance;
            }).collect(toList());
        } catch (JDOMException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void simulate();

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

}
