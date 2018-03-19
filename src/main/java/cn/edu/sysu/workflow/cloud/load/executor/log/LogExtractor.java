package cn.edu.sysu.workflow.cloud.load.executor.log;

import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.SimulatableProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.Task;
import com.google.common.collect.ImmutableList;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

//import sun.awt.util.IdentityArrayList;

public class LogExtractor {

    public List<ProcessInstance> extractProcessInstance(File file) {

        List<ProcessInstance> instanceList = new ArrayList<>();
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

                List<Task> tasks = new ArrayList<>();
                assignMap.forEach((s, elements) -> {
                    elements.sort(Comparator.comparingLong(timeStampMap::get));
                    List<Element> completeElements = completeMap.get(s);
                    completeElements.sort(Comparator.comparingLong(timeStampMap::get));
                    if (elements.size() != completeElements.size()) {
                        throw new RuntimeException("assign and complete not match");
                    }
                    for (int i = 0; i < elements.size(); i++) {
                        Task task = new Task();
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
                tasks.sort(Comparator.comparingLong(Task::getStart));
                for (int i = 0; i < tasks.size(); i++) {
                    if (tasks.get(i).getEnd() == 0) {
                        if (i < tasks.size() - 1) {
                            tasks.get(i).setEnd(tasks.get(i + 1).getStart() - 100);
                        } else {
                            tasks.get(i).setEnd(tasks.get(i).getStart() + 100);
                        }
                    }
                }
                SimulatableProcessInstance processInstance = new SimulatableProcessInstance();
                processInstance.setDefinitionId(file.getName().substring(0, file.getName().indexOf('.')));
//                System.out.println(instanceList.size());
                processInstance.setTasks(tasks);
                instanceList.add(processInstance);
            });


        } catch (JDOMException | IOException e) {
            throw new RuntimeException(e);
        }
        return ImmutableList.copyOf(instanceList);
    }

    long parseTimeStampString(String timeStampString) {
        timeStampString=timeStampString.substring(0,timeStampString.length()-10);
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date timeStamp;
        try {
            timeStamp = format.parse(timeStampString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if (timeStamp == null) {
            return 0;
        }
        return timeStamp.getTime();
    }
    private long getTimeStampFromElement(Element element) {
        String timeStampString = element.getChildText("Timestamp");
        return parseTimeStampString(timeStampString);
    }


}
