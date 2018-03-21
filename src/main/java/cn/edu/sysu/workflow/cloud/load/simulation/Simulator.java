package cn.edu.sysu.workflow.cloud.load.simulation;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.algorithm.scheduling.*;
import cn.edu.sysu.workflow.cloud.load.balance.LoadBalancer;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.graph.GraphUtil;
import cn.edu.sysu.workflow.cloud.load.log.LogExtractor;
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
//        }
//        (Arrays.stream(processDefinitionFiles).parallel()).forEach(file -> {


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


//        BasicEngine[] workflowEngines = new SimulateActiviti[Constant.ENGINE_NUMBER];
//        for (int i = 0; i < workflowEngines.length; i++) {
//            workflowEngines[i] = new SimulateActiviti(Constant.ENGINE_CAPACITY, new HttpConfig());
//        }
//        loadBalancer = new LoadBalancer(workflowEngines, Constant.ENGINE_CAPACITY);
//        simulateEnvironment = new SimulateEnvironment(SimulateConstant.TOTAL);
    }


    public SimulateResult simulate(LoadBalancer loadBalancer, int number) {
        int count = 0;
        int current = 0;
        SimulateEnvironment simulateEnvironment = new SimulateEnvironment(SimulateConstant.TOTAL);

        SimulateResult result = new SimulateResult();

        while (count < number && current < (SimulateConstant.TOTAL)) {
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

    class SimulateResult {
        int maxEngine;
        int[][] utilizations;
    }



    private Scheduler[] schedulers = new Scheduler[] { new FirstFitScheduler(), new RealFirstFitScheduler(), new BestFitScheduler(), new FFDScheduler()};
    private Map<Scheduler, Map<Integer, Integer>> testUsedEngines() {

        Integer[] trace = new Integer[20];
        for(int i = 0; i < trace.length; i++) {
            trace[i] = (i + 1) * (2000 / trace.length);
        }

        Map<Scheduler, Map<Integer, Integer>> result = new HashMap<>();

        Arrays.stream(schedulers).forEach(scheduler -> result.put(scheduler, new TreeMap<>()));

        SimulateResult[] results = new SimulateResult[schedulers.length];
        Arrays.stream(trace).forEach(new Consumer<Integer>() {
            @Override
            public void accept(Integer number) {
                for(int i = 0; i < schedulers.length; i++) {
                    System.out.println("simulate start " + schedulers[i].getClass().getName());

                    results[i] = simulate(LoadBalancerGenerator.INSTANCE.testScheduling(schedulers[i]), number);
                    result.get(schedulers[i]).put(number, results[i].maxEngine);
                    System.out.println("simulate" + schedulers[i].getClass().getName());
                }
                System.out.println("iterate " + String.valueOf(number));
            }
        });
        return result;
    }

    private Map<Scheduler, Map<Integer, Double>> testUsedEngine() {

        Map<Scheduler, Map<Integer, Double>> result = new HashMap<>();

        for(int i = 0; i < schedulers.length; i++) {
            result.put(schedulers[i], new TreeMap<>());
            SimulateResult sr = simulate(
                    LoadBalancerGenerator.INSTANCE.testScheduling(schedulers[i]), 300);

            int maxV = 0;
            for(int j = 0; j < 10; j++) {
                int count = 0;
                for(int k = 0; k < sr.utilizations.length; k++) {
                    if(sr.utilizations[k][j] != 0) {
                        count++;
                    }
                }
                maxV =  Math.max(maxV, count);
                result.get(schedulers[i]).put(j, Double.valueOf(maxV));
            }
        }
        return result;
    }

    private Map<Scheduler, Map<Integer, Double>> testUtilization() {

        Map<Scheduler, Map<Integer, Double>> result = new HashMap<>();

        for(int i = 0; i < schedulers.length; i++) {
            result.put(schedulers[i], new TreeMap<>());
            SimulateResult sr = simulate(
                    LoadBalancerGenerator.INSTANCE.testScheduling(schedulers[i]), 300);

            int[] sums = new int[sr.utilizations[0].length];
            int[] capacity = new int[sr.utilizations[0].length];
            Arrays.fill(capacity, Constant.ENGINE_CAPACITY * sr.utilizations.length);
            for(int j = 0; j < 20; j++) {
                int all = 0;
                for(int k = 0; k < sr.utilizations.length; k++) {
                    all += sr.utilizations[k][j];
                }
                sums[j] = all;
                result.get(schedulers[i]).put(j, Math.min(1d, ((double) all / Constant.ENGINE_CAPACITY * sr.utilizations.length)));
            }

            for(int j = 0; j < 20; j++) {
                result.get(schedulers[i]).put(j, Math.min(1d, ((double) sums[j] / capacity[j])));
            }

            System.out.println(sums);
        }
        return result;
    }

    private Map<Scheduler, Map<Integer, Double>> testSLA() {

        Map<Scheduler, Map<Integer, Double>> result = new HashMap<>();

        for(int i = 0; i < schedulers.length; i++) {
            result.put(schedulers[i], new TreeMap<>());
            SimulateResult sr = simulate(
                    LoadBalancerGenerator.INSTANCE.testScheduling(schedulers[i]), 300);

            int maxV = 0;
            for(int j = 0; j < 10; j++) {
                int count = 0;
                int all = 0;
                for(int k = 0; k < sr.utilizations.length; k++) {
                    if(sr.utilizations[k][j] > Constant.ENGINE_CAPACITY) {
                        count += sr.utilizations[k][j] - Constant.ENGINE_CAPACITY;
                    }
                    all += sr.utilizations[k][j];
                }
                result.get(schedulers[i]).put(j, Double.valueOf(count) / all);
            }
        }
        return result;
    }

    public static void main(String[] args) {

        Map<Scheduler, Map<Integer, Double>> result = new Simulator().testUtilization();

        ApplicationFrame frame = GraphUtil.getFrame(result);

        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }
}
