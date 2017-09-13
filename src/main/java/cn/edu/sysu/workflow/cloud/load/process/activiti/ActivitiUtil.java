package cn.edu.sysu.workflow.cloud.load.process.activiti;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Component
public class ActivitiUtil {

    public List<SequenceFlow> getFollowingFlows(String taskName, BpmnModel bpmnModel) {
        List<SequenceFlow> result = new ArrayList<>();
        bpmnModel.getMainProcess().getFlowElements().parallelStream()
                .filter(flowElement -> taskName.equals(flowElement.getName())).findFirst().ifPresent(new Consumer<FlowElement>() {
            @Override
            public void accept(FlowElement flowElement) {
                bpmnModel.getMainProcess().getFlowElements().stream()
                        .filter(innerFlowElement -> innerFlowElement instanceof SequenceFlow &&
                                ((SequenceFlow) innerFlowElement).getSourceRef().equals(flowElement.getId()))
                        .forEach(flowElement1 -> result.add(((SequenceFlow) flowElement1)));
            }
        });
        return result;
    }

    public Map<String, Object> getNextTaskVariables(BpmnModel bpmnModel, String taskName, String nextTaskName) {
        Map<String, Object> variables = new HashMap<>();
        getFollowingFlows(taskName, bpmnModel).stream()
                .filter(sequenceFlow -> nextTaskName.equals(bpmnModel.getMainProcess()
                        .getFlowElement(sequenceFlow.getTargetRef())))
                .findFirst().ifPresent(sequenceFlow -> {
           String expression =  sequenceFlow.getConditionExpression();
           expression = StringUtils.remove(expression, ' ');
           expression = expression.substring(2, expression.length() - 1);
           String[] variableStrings = expression.split("&&");
            Arrays.stream(variableStrings).forEach(s -> {
                String[] elements = s.split("==");
                if( elements.length < 2) {
                    throw new RuntimeException("el expression is not valid");
                }
                variables.put(elements[0], elements[1]);
            });
        });
        return variables;
    }
}
