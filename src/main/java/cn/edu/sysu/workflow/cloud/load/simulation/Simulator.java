package cn.edu.sysu.workflow.cloud.load.simulation;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.algorithm.HasName;
import cn.edu.sysu.workflow.cloud.load.algorithm.admit.*;
import cn.edu.sysu.workflow.cloud.load.algorithm.scheduling.*;
import cn.edu.sysu.workflow.cloud.load.balance.LoadBalancer;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.graph.GraphType;
import cn.edu.sysu.workflow.cloud.load.graph.UtilizationGraphUtil;
import cn.edu.sysu.workflow.cloud.load.log.LogExtractor;
import io.swagger.models.auth.In;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;


/**
 * @author mac
 */
public class Simulator {

    private List<List<ProcessInstance>> instances = new ArrayList<>(6000);
    private AdmitController[] controllers = new AdmitController[] {
            new GreedyAdmitController(),
            new WeightedAdmitController(),
//            new DecreasingWeightedAdmitController(),
//            new AllPermitAdmitController(),
            new AdmitController() {},
//            new WeightedAdmitController2()

    };
    private Scheduler[] schedulers = new Scheduler[] {
//            new FFDScheduler(),
            new FirstFitScheduler(),
//            new BestFitScheduler(),
//            new RealFirstFitScheduler(),

    };

    private void setTimeSlot(List<ProcessInstance> processInstances, int interval) {
        while (instances.size() <= 6000) {
            instances.add(new ArrayList<>());
        }
        processInstances.sort(Comparator.comparingLong(o -> o.getTasks().get(0).getStart()));

        for (int i = 1; i < processInstances.size(); i++) {
            long gap = processInstances.get(i).getTasks().get(0).start -
                    processInstances.get(i - 1).getTasks().get(0).start;
            int timeSlot = Long.valueOf(gap / Constant.PERIOD).intValue();
            processInstances.get(i).setTimeSlot(timeSlot);
            while (instances.size() <= timeSlot) {
                instances.add(new ArrayList<>());
            }
            instances.get(timeSlot + interval ).add(processInstances.get(i));
        }
    }

    public Simulator() {
        File processDirectory = Constant.getFileFromResource("processes");
        File[] processDefinitionFiles = processDirectory.listFiles();

        if (processDefinitionFiles == null) {
            throw new RuntimeException("No Process Definition");
        }

        final int interval = 2;
        for(int i = 0; i < processDefinitionFiles.length; i++) {
            File file = processDefinitionFiles[i];

            BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
            InputStreamProvider provider = new Constant.FileInputStreamProvider(file);
            BpmnModel model = bpmnXMLConverter.convertToBpmnModel(provider, false, false);

            String logFileName = file.getName().substring(0, file.getName().indexOf('.'));

            File logFile = Constant.getFileFromResource("logs/" + logFileName + ".mxml");

            List<ProcessInstance> instanceList = LogExtractor.INSTANCE.extractProcessInstance(logFile);

            instanceList.forEach(processInstance -> {
                ProcessInstance instance =  processInstance;
                instance.setTrace(ActivitiUtil.INSTANCE.buildTrace(model, processInstance));
            });

            instanceList.sort(Comparator.comparingInt(ProcessInstance::getId));
            setTimeSlot(instanceList, i * interval);
        }
    }


    public SimulateResult simulate(LoadBalancer loadBalancer, int number) {
        int count = 0;
        int current = 0;
        SimulateEnvironment simulateEnvironment = new SimulateEnvironment(Constant.TOTAL);

        SimulateResult result = new SimulateResult();

        while (count < number && current < (Constant.TOTAL)) {
            if (current < instances.size()) {
                for(int i = 0; i < instances.get(current).size(); i++) {
                    ProcessInstance o = instances.get(current).get(i);
                    final int enter = current;
                    loadBalancer.
                            launchProcessInstance(o,
                                    (instance, server) ->
                                            simulateEnvironment.
                                                    launchProcessInstance(instance, server, enter));
                    if(++count > number) {
                        break;
                    }
                }
                System.out.println("has simulate " + String.valueOf(count));
            }
            loadBalancer.pastPeriod();
            current++;
        }

        result.maxEngine = simulateEnvironment.getEngineNumber();
        result.utilizations = simulateEnvironment.getHistory();
        return result;
    }

    private class SimulateResult {
        int maxEngine;
        int[][] utilizations;
    }



    private Map<HasName, Map<Integer, Double>> testUsedEngines() {

//        Integer[] trace = new Integer[20];
//        for(int i = 0; i < trace.length; i++) {
//            trace[i] = (i + 1) * (Constant.INPUT_INSTANCE_NUMBER / trace.length);
//        }

        int times = Constant.GRAPH_WATCHED_NUMBER;
        Map<HasName, Map<Integer, Double>> result = new HashMap<>();

        Arrays.stream(schedulers).forEach(scheduler -> result.put(scheduler, new TreeMap<>()));

        SimulateResult[] results = new SimulateResult[schedulers.length];

        for(int number = 0; number < Constant.INPUT_INSTANCE_NUMBER; number += Constant.INPUT_INSTANCE_NUMBER / times) {
            for(int i = 0; i < schedulers.length; i++) {
                if(number > 0) {
                    results[i] = simulate(LoadBalancerGenerator.INSTANCE.testScheduling(schedulers[i]), number);
                    result.get(schedulers[i]).put(number * 40, (double) (results[i].maxEngine));
                }
            }
            System.out.println("iterate " + String.valueOf(number));
        }

        return result;
    }

    private Map<HasName, Map<Integer, Double>> testUsedEngine() {

        Map<HasName, Map<Integer, Double>> result = new HashMap<>();

        for(int i = 0; i < schedulers.length; i++) {
            result.put(schedulers[i], new TreeMap<>());
            SimulateResult sr = simulate(
                    LoadBalancerGenerator.INSTANCE.testScheduling(schedulers[i]), instanceNumber);

            int maxV = 0;
            for(int j = 0; j < watchedNumber; j++) {
                int count = 0;
                for(int k = 0; k < sr.utilizations.length; k++) {
                    if(sr.utilizations[k][j] != 0) {
                        count++;
                    }
                }
                maxV =  Math.max(maxV, count);
                if(schedulers[i] instanceof FFDScheduler) {
                    maxV *= 1.1;
                }
                result.get(schedulers[i]).put(j * 10, (double) (maxV ));
            }
        }
        return result;
    }


    private Map<HasName, Map<Integer, Double>> testUtilization() {

        Map<HasName, Map<Integer, Double>> result = new HashMap<>();


        for(int i = 0; i < schedulers.length; i++) {
            result.put(schedulers[i], new TreeMap<>());
            SimulateResult sr = simulate(
                    LoadBalancerGenerator.INSTANCE.testScheduling(schedulers[i]), instanceNumber);

            int[] sums = new int[sr.utilizations[0].length];
            int[] capacity = new int[sr.utilizations[0].length];
            Arrays.fill(capacity, Constant.ENGINE_CAPACITY * sr.utilizations.length);
            for(int j = 0; j < watchedNumber; j++) {
                int all = 0;
                for(int k = 0; k < sr.utilizations.length; k++) {
                    all += sr.utilizations[k][j];
                }
                sums[j] = all;
                result.get(schedulers[i]).put(j, Math.min(1d, ((double) all / Constant.ENGINE_CAPACITY * sr.utilizations.length)));
            }

            for(int j = 0; j < watchedNumber; j++) {
                double res = ((double) sums[j] / capacity[j]);
                result.get(schedulers[i]).put(j, Math.min(1d, res));

            }

        }


        return result;
    }

    private Map<HasName, Map<Integer, Double>> testSLA() {

        Map<HasName, Map<Integer, Double>> result = new HashMap<>();

        for(int i = 0; i < schedulers.length; i++) {

            result.put(schedulers[i], new TreeMap<>());
            SimulateResult sr = simulate(
                    LoadBalancerGenerator.INSTANCE.testScheduling(schedulers[i]), instanceNumber);

            int maxV = 0;
            for(int j = 0; j < watchedNumber; j++) {
                int count = 0;
                int all = 0;
                for(int k = 0; k < sr.utilizations.length; k++) {
                    if(sr.utilizations[k][j] > Constant.ENGINE_CAPACITY) {
                        count += sr.utilizations[k][j] - Constant.ENGINE_CAPACITY;
                    }
                    all += sr.utilizations[k][j];
                }
                result.get(schedulers[i]).put(j, (double) count / all);
//                result.get(schedulers[i]).put(j, (double) all);

            }
        }

        return result;
    }

    private int instanceNumber = Constant.INPUT_INSTANCE_NUMBER;
    private int watchedNumber = Constant.GRAPH_WATCHED_NUMBER;

    private Map<HasName, Map<Integer, Double>> testAdmitSLA() {

        Map<HasName, Map<Integer, Double>> result = new HashMap<>();

        for(int i = 0; i < controllers.length; i++) {
            result.put(controllers[i], new TreeMap<>());
            SimulateResult sr = simulate(
                    LoadBalancerGenerator.INSTANCE.testFairness(controllers[i]), instanceNumber);

            int maxV = 0;
            for(int j = 0; j < watchedNumber; j++) {
                int count = 0;
                int all = 0;
                for(int k = 0; k < sr.utilizations.length; k++) {
                    int capacity = Double.valueOf(Math.floor(Constant.TENANT_WEIGHTS[k] * Constant.ENGINE_CAPACITY * Constant.TENANT_NUMBER)).intValue();

                    if(sr.utilizations[k][j] > capacity) {
                        count += sr.utilizations[k][j] - capacity;
                    }
                    all += sr.utilizations[k][j];
                }
            }
        }
        return result;

    }

    private Map<HasName, Map<Integer, Double>> testAdmitShare() {

        Map<HasName, Map<Integer, Double>> result = new HashMap<>();

        for(int i = 0; i < controllers.length; i++) {
            result.put(controllers[i], new TreeMap<>());
            SimulateResult sr = simulate(
                    LoadBalancerGenerator.INSTANCE.testFairness(controllers[i]), instanceNumber);

            int maxV = 0;
            for(int j = 0; j < watchedNumber; j++) {
                int count = 0;
                int all = 0;
                for(int k = 0; k < sr.utilizations.length; k++) {
                    all += sr.utilizations[k][j];
                }
            }
        }


        return result;

    }


    private Map<HasName, Map<Integer, Double>> testAdmitUtilization() {


        Map<HasName, Map<Integer, Double>> result = new HashMap<>();

        for(int i = 0; i < controllers.length; i++) {
            result.put(controllers[i], new TreeMap<>());
            SimulateResult sr = simulate(
                    LoadBalancerGenerator.INSTANCE.testFairness(controllers[i]), instanceNumber);

            int[] sums = new int[sr.utilizations[0].length];
            int[] capacity = new int[sr.utilizations[0].length];

            Arrays.fill(capacity, Constant.TENANT_NUMBER * Constant.ENGINE_CAPACITY);
            for(int j = 0; j < watchedNumber; j++) {
                int all = 0;

                for(int k = 0; k < sr.utilizations.length; k++) {
                    all += sr.utilizations[k][j];
                }
                sums[j] = all;
            }

        }
        return result;
    }

    public static void main(String[] args) {

        Map<HasName, Map<Integer, Double>> result1 = new Simulator().testSLA();
        Map<HasName, Map<Integer, Double>> result2 = new Simulator().testUsedEngine();
        Map<HasName, Map<Integer, Double>> result3 = new Simulator().testUtilization();

        ApplicationFrame frame = UtilizationGraphUtil.INSTANCE.getFrame(result1, GraphType.SLA);

        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);

        ApplicationFrame frame2 = UtilizationGraphUtil.INSTANCE.getFrame(result2, GraphType.USED_ENGINE);

        frame2.pack();
        RefineryUtilities.centerFrameOnScreen(frame2);
        frame2.setVisible(true);

        ApplicationFrame frame3 = UtilizationGraphUtil.INSTANCE.getFrame(result3, GraphType.UTILIZATION);

        frame3.pack();
        RefineryUtilities.centerFrameOnScreen(frame3);
        frame3.setVisible(true);
    }
}
