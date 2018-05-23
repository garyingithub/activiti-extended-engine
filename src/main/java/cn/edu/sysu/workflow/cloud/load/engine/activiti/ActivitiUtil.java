package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.data.Task;
import cn.edu.sysu.workflow.cloud.load.data.TraceNode;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public enum ActivitiUtil {

    INSTANCE;
    private final String EXTENDED_PREFIX = "/extended";

    public String buildClaimUrl(HttpConfig httpConfig, String instanceId, String taskName) {

        return String.format("%s%s%s%s%s",
                httpConfig.getAddress(),
                EXTENDED_PREFIX,
                "/claimTask",
                encodePathVariable(instanceId),
                encodePathVariable(taskName));
    }

    public String buildCompleteUrl(HttpConfig httpConfig, String instanceId, String taskName) {

        return String.format("%s%s%s%s%s",
                httpConfig.getAddress(),
                EXTENDED_PREFIX,
                "/completeTask",
                encodePathVariable(instanceId),
                encodePathVariable(taskName));
    }

    public String buildStartProcessUrl(HttpConfig httpConfig, ProcessInstance processInstance) {

        return String.format("%s%s%s%s",
                httpConfig.getAddress(),
                EXTENDED_PREFIX,
                "/startProcess",
                encodePathVariable(processInstance.getDefinitionId()));
    }

    private String encodePathVariable(String pathVariable) {
        try {
            return "/".concat(URLEncoder.encode(pathVariable, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String buildDeployDefinitionUrl(HttpConfig httpConfig) {
        return String.format("%s%s", httpConfig.getAddress(), "/repository/deployments");
    }

    /**
     * 将形如${number == 3} 的el表达式转化为Map, 目前仅支持==
     *
     * @param expression 表达式
     * @return 符合条件要求的变量Map
     */
    private Map<String, Object> parseElExpression(String expression) {
        if (StringUtils.isBlank(expression)) {
            return null;
        }
        Map<String, Object> variables = new HashMap<>();
        expression = StringUtils.remove(expression, ' ');
        expression = expression.substring(2, expression.length() - 1);
        String[] variableStrings = expression.split("&&");
        Arrays.stream(variableStrings).forEach(s -> {
            String[] elements = s.split("==");
            if (elements.length < 2) {
                throw new RuntimeException("el expression is not valid");
            }
            variables.put(elements[0], elements[1]);
        });
        return variables;
    }

    private Map<FlowElement, List<SequenceFlow>> getTaskFlowsMap(BpmnModel bpmnModel) {
        Map<FlowElement, List<SequenceFlow>> result = new HashMap<>();
        Process mainProcess = bpmnModel.getMainProcess();
        bpmnModel.getMainProcess().getFlowElements()
                .stream()
                .filter(flowElement -> flowElement instanceof SequenceFlow)
                .forEach(flowElement -> {
                    SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
                    result.putIfAbsent(mainProcess.getFlowElement(sequenceFlow.getSourceRef()), new ArrayList<>());
                    result.get(mainProcess.getFlowElement(sequenceFlow.getSourceRef())).add(sequenceFlow);
                });
        return result;
    }

    public TraceNode buildTrace(BpmnModel model, ProcessInstance instance) {
        Optional<FlowElement> flowElementOptional = model.getMainProcess().getFlowElements().stream().filter(flowElement -> flowElement instanceof StartEvent).findFirst();
        if (!flowElementOptional.isPresent()) {
            throw new RuntimeException("Can't find first flowElement");
        }
        return build(model, getTaskFlowsMap(model), flowElementOptional.get(), instance.getTasks());
    }

    private TraceNode getCurrentNode(FlowElement startElement, List<Task> tasks) {
        if (startElement instanceof StartEvent) {
            TraceNode result = new TraceNode();
            result.setNextNodes(new ArrayList<>());
            result.setVariables(new HashMap<>());
            return result;
        } else {
            if (!(startElement instanceof UserTask) && !(startElement instanceof ServiceTask)) {
                throw new RuntimeException("UnSupported Type of Event " + startElement.getClass().getName());
            }
        }

        int start = 0;
        for (; start < tasks.size(); start++) {
            Task task = tasks.get(start);
            if (task.isAvailable() && task.getTaskName().equals(startElement.getName())) {
                break;
            }
        }
        if (start == tasks.size()) {
            throw new RuntimeException("can not find start element in logs");
        }
        TraceNode root = new TraceNode();
        root.setTask(tasks.get(start));
        tasks.get(start).setAvailable(false);
        root.setNextNodes(new ArrayList<>());
        return root;
    }


    // 使用深度优先搜索查找任务
    private void getFollowingSequenceFlowsByTaskName(Process process, Gateway gateway, String taskName, List<SequenceFlow> result, List<SequenceFlow> temp) {
        // TODO Exclusive Gateway不能处理多个变量
        for (SequenceFlow sequenceFlow : gateway.getOutgoingFlows()) {
            temp.add(sequenceFlow);
            FlowElement nextElement = process.getFlowElement(sequenceFlow.getTargetRef());
            if (nextElement instanceof UserTask) {
                if (taskName.equals(nextElement.getName())) {
                    if (result.size() == 0 || temp.size() < result.size()) {
                        result.clear();
                        result.addAll(temp);
                    }
                }
            } else {
                if (nextElement instanceof Gateway) {
                    getFollowingSequenceFlowsByTaskName(process, (Gateway) nextElement, taskName, result, temp);
                } else {
                    throw new RuntimeException("Not supported here");
                }
            }
            temp.remove(temp.size() - 1);
        }
    }

    private TraceNode build(
            BpmnModel bpmnModel,
            Map<FlowElement, List<SequenceFlow>> flowMap,
            FlowElement startElement,
            List<Task> tasks) {

        TraceNode root = getCurrentNode(startElement, tasks);
        SequenceFlow followingSequence = flowMap.get(startElement).get(0);
        FlowElement nextFlow = bpmnModel.getMainProcess().getFlowElement(followingSequence.getTargetRef());
        if (nextFlow instanceof ExclusiveGateway) {
            for (int i = 0; i < tasks.size(); i++) {
                if (!tasks.get(i).isAvailable()) {
                    continue;
                }
                for (SequenceFlow sequenceFlow : flowMap.get(nextFlow)) {
                    FlowElement nextElement = bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef());
                    if (nextElement instanceof Gateway) {
                        List<SequenceFlow> sequenceFlows = new ArrayList<>();
                        getFollowingSequenceFlowsByTaskName(bpmnModel.getMainProcess(), (Gateway) nextElement, tasks.get(i).getTaskName(), sequenceFlows, new ArrayList<>());
                        if (sequenceFlows.size() > 0) {
                            root.getNextNodes().add(build(bpmnModel, flowMap, bpmnModel.getMainProcess().getFlowElement(sequenceFlows.get(sequenceFlows.size() - 1).getTargetRef()), tasks));
                            Map<String, Object> allConditions = new HashMap<>();
                            sequenceFlows.forEach(sequenceFlow1 -> {
                                if (StringUtils.isNoneBlank(sequenceFlow1.getConditionExpression())) {
                                    parseElExpression(sequenceFlow1.getConditionExpression()).forEach(allConditions::put);
                                }
                            });

                            root.setVariables(allConditions);

                            if (StringUtils.isNoneBlank(sequenceFlow.getConditionExpression())) {
                                if (root.getVariables() == null) {
                                    root.setVariables(new HashMap<>());
                                }
                                parseElExpression(sequenceFlow.getConditionExpression()).forEach((s, o) -> root.getVariables().put(s, o));
                            }

                            return root;
                        }
                    }
                    if ((tasks.get(i).getTaskName().equals(bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef()).getName()))) {
                        root.getNextNodes().add(build(bpmnModel, flowMap, bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef()), tasks));
                        root.setVariables(parseElExpression(sequenceFlow.getConditionExpression()));
                        return root;
                    }
                }
            }
            throw new RuntimeException("Must be at least one choice");
        } else {
            if (nextFlow instanceof ParallelGateway) {
                for (SequenceFlow sequenceFlow : flowMap.get(nextFlow)) {
                    if (StringUtils.isBlank(sequenceFlow.getConditionExpression())) {
                        root.getNextNodes().add(build(bpmnModel, flowMap, bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef()), tasks));
                    }
                }
            } else {
                if ((nextFlow instanceof UserTask)) {
                    root.getNextNodes().add(build(bpmnModel, flowMap, nextFlow, tasks));
                }
            }
        }
        return root;
    }


}
