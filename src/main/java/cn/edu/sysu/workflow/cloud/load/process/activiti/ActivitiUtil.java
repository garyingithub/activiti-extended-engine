package cn.edu.sysu.workflow.cloud.load.process.activiti;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.Task;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActivitiUtil {

//    public List<SequenceFlow> getFollowingFlows(String taskName, BpmnModel bpmnModel) {
//        List<SequenceFlow> result = new ArrayList<>();
//        bpmnModel.getMainProcess().getFlowElements().parallelStream()
//                .filter(flowElement -> taskName.equals(flowElement.getName())).findFirst().ifPresent(new Consumer<FlowElement>() {
//            @Override
//            public void accept(FlowElement flowElement) {
//                bpmnModel.getMainProcess().getFlowElements().stream()
//                        .filter(innerFlowElement -> innerFlowElement instanceof SequenceFlow &&
//                                ((SequenceFlow) innerFlowElement).getSourceRef().equals(flowElement.getId()))
//                        .forEach(flowElement1 -> result.add(((SequenceFlow) flowElement1)));
//            }
//        });
//        return result;
//    }
//
//    public Map<String, Object> getNextTaskVariables(BpmnModel bpmnModel, String taskName, String nextTaskName) {
//        Map<String, Object> variables = new HashMap<>();
//        getFollowingFlows(taskName, bpmnModel).stream()
//                .filter(sequenceFlow -> nextTaskName.equals(bpmnModel.getMainProcess()
//                        .getFlowElement(sequenceFlow.getTargetRef())))
//                .findFirst().ifPresent(sequenceFlow -> {
//           String expression =  sequenceFlow.getConditionExpression();
//           expression = StringUtils.remove(expression, ' ');
//           expression = expression.substring(2, expression.length() - 1);
//           String[] variableStrings = expression.split("&&");
//            Arrays.stream(variableStrings).forEach(s -> {
//                String[] elements = s.split("==");
//                if( elements.length < 2) {
//                    throw new RuntimeException("el expression is not valid");
//                }
//                variables.put(elements[0], elements[1]);
//            });
//        });
//        return variables;
//    }

    /**
     * 将形如${number == 3} 的el表达式转化为Map, 目前仅支持==
     *
     * @param variables
     * @param expression
     * @return
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

    public Map<String, Map<String, Object>> parseForVariables(BpmnModel bpmnModel) {
        Process mainProcess = bpmnModel.getMainProcess();
        Map<String, Map<String, Object>> result = new HashMap<>();
        mainProcess.getFlowElements().stream().filter(flowElement -> flowElement instanceof SequenceFlow && StringUtils.isNoneBlank(((SequenceFlow) flowElement).getConditionExpression()))
                .forEach(flowElement -> {
                    SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
                    String taskName = mainProcess.getFlowElement(sequenceFlow.getTargetRef()).getName();
                    if (StringUtils.isNoneBlank(sequenceFlow.getConditionExpression())) {
                        result.put(taskName, parseElExpression(sequenceFlow.getConditionExpression()));
                    }
                });
        return result;
    }
}
