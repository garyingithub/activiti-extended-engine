package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import cn.edu.sysu.workflow.cloud.load.engine.TraceNode;
import cn.edu.sysu.workflow.cloud.load.simulator.ProcessInstance;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ActivitiUtil {
    /**
     * 将形如${number == 3} 的el表达式转化为Map, 目前仅支持==
     *
     * @param expression 表达式
     * @return 符合条件要求的变量Map
     */
    private Map<String, Object> parseElExpression(String expression) {
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
        Optional<FlowElement> flowElementOptional = model.getMainProcess().getFlowElements().stream().filter(flowElement -> flowElement.getId().contains("startEvent")).findFirst();
        if (!flowElementOptional.isPresent()) {
            throw new RuntimeException("Can't find first flowElement");
        }
        return build(model, getTaskFlowsMap(model), flowElementOptional.get(), instance.getTasks());
    }

    private TraceNode getCurrentNode(FlowElement startElement, List<ProcessInstance.Task> tasks) {
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
            ProcessInstance.Task task = tasks.get(start);
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

    private TraceNode build(
            BpmnModel bpmnModel,
            Map<FlowElement, List<SequenceFlow>> flowMap,
            FlowElement startElement,
            List<ProcessInstance.Task> tasks) {

        TraceNode root = getCurrentNode(startElement, tasks);
        SequenceFlow followingSequence = flowMap.get(startElement).get(0);
        FlowElement nextFlow = bpmnModel.getMainProcess().getFlowElement(followingSequence.getTargetRef());
        if (nextFlow instanceof ExclusiveGateway) {
            for (int i = 0; i < tasks.size(); i++) {
                if (!tasks.get(i).isAvailable()) continue;
                for (SequenceFlow sequenceFlow : flowMap.get(nextFlow)) {
                    if (bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef()).getName().equals(tasks.get(i).getTaskName())) {
                        root.getNextNodes().add(build(bpmnModel, flowMap, bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef()), tasks));
                        root.setVariables(parseElExpression(sequenceFlow.getConditionExpression()));
                        return root;
                    }
                }
            }
        } else {
            if (nextFlow instanceof ParallelGateway) {
                for (SequenceFlow sequenceFlow : flowMap.get(nextFlow)) {
                    if (StringUtils.isBlank(sequenceFlow.getConditionExpression())) {
                        root.getNextNodes().add(build(bpmnModel, flowMap, bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef()), tasks));
                    }
                }
            } else {
                if (!(nextFlow instanceof EndEvent)) {
                    root.getNextNodes().add(build(bpmnModel, flowMap, nextFlow, tasks));
                }
            }
        }
        return root;
    }


}
