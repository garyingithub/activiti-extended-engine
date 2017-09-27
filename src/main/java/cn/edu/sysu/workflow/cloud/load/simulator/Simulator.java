package cn.edu.sysu.workflow.cloud.load.simulator;

import cn.edu.sysu.workflow.cloud.load.engine.ProcessEngine;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;


public abstract class Simulator {
    private ProcessEngine engine;
    private File logFile;

    protected List<ProcessInstance> instanceList = new ArrayList<>();
    private SimulatorUtil simulatorUtil = new SimulatorUtil();

    protected Simulator() {

    }

//    private Random random = new Random();

    // 日志中有些complete在assign之前。需要特殊处理
    public Simulator(File file) {
        this.logFile = file;
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(file);
            Element root = document.getRootElement();
            Element process = root.getChild("Process");


            process.getChildren("ProcessInstance").forEach(o -> {
                Element element = (Element) o;
                Map<String, List<Element>> assignMap = new HashMap<>();
                Map<String, List<Element>> completeMap = new HashMap<>();
                Map<Element, Long> timeStampMap = new HashMap<>();

                for (Object taskObject : element.getChildren("AuditTrailEntry")) {
                    Element taskElement = (Element) taskObject;
                    String taskName = taskElement.getChildText("WorkflowModelElement");
                    String taskEvent = taskElement.getChildText("EventType");
//                    String timeStampString = taskElement.getChildText("Timestamp");
                    String originator = taskElement.getChildText("Originator");
                    if (!taskName.contains("EVENT") && !taskName.equals("subProcess") && !(originator != null && originator.equals("Automated Service"))) {
                        if (taskEvent.equals("assign")) {
                            assignMap.putIfAbsent(taskName, new ArrayList<>());
                            assignMap.get(taskName).add(taskElement);
                            timeStampMap.put(taskElement, getTimeStampFromElement(taskElement));
                        } else {
                            if (taskEvent.equals("complete")) {
                                completeMap.putIfAbsent(taskName, new ArrayList<>());
                                completeMap.get(taskName).add(taskElement);
                                timeStampMap.put(taskElement, getTimeStampFromElement(taskElement));
                            } else {
                                throw new RuntimeException("Not supported event");
                            }
                        }

                    }
                }

                List<ProcessInstance.Task> tasks = new ArrayList<>();
                assignMap.forEach((s, elements) -> {
                    elements.sort(Comparator.comparingLong(timeStampMap::get));
                    List<Element> completeElements = completeMap.get(s);
                    completeElements.sort(Comparator.comparingLong(timeStampMap::get));
                    if (elements.size() != completeElements.size()) {
                        throw new RuntimeException("assign and complete not match");
                    }
                    for (int i = 0; i < elements.size(); i++) {
                        ProcessInstance.Task task = new ProcessInstance.Task();
                        task.setAvailable(true);
                        task.setStart(getTimeStampFromElement(elements.get(i)));
                        task.setEnd(getTimeStampFromElement(completeElements.get(i)));
                        if (task.getEnd() < task.getStart()) {
//                            throw new RuntimeException("end must be smaller than start");
                            task.setEnd(0);
                        }
                        task.setTaskName(elements.get(i).getChildText("WorkflowModelElement"));
                        tasks.add(task);
                    }
                });
                tasks.sort(Comparator.comparingLong(ProcessInstance.Task::getStart));
                for (int i = 0; i < tasks.size(); i++) {
                    if (tasks.get(i).getEnd() == 0) {
                        if (i < tasks.size() - 1) {
                            tasks.get(i).setEnd(tasks.get(i + 1).getStart() - 100);
                        } else {
                            tasks.get(i).setEnd(tasks.get(i).getStart() + 100);
                        }
                    }
                }
                ProcessInstance processInstance = new ProcessInstance();
                processInstance.setDefinitionId(file.getName().substring(0, file.getName().indexOf('.')));
//                System.out.println(instanceList.size());
                processInstance.setTasks(tasks);
                instanceList.add(processInstance);
            });


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

    protected File getLogFile() {
        return logFile;
    }

    private long getTimeStampFromElement(Element element) {
        String timeStampString = element.getChildText("Timestamp");
        return simulatorUtil.parseTimeStampString(timeStampString);
    }


}
