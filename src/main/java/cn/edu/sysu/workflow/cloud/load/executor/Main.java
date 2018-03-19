package cn.edu.sysu.workflow.cloud.load.executor;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.executor.log.LogExtractor;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.simulator.activiti.ActivitiSimuluator;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.SimulatableProcessInstance;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {

        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setHost(Constant.ENGINE_ADDRESS);
        httpConfig.setPort(Constant.PORTS[0]);

        LoadBalancer loadBalancer = new LoadBalancer(new Activiti[] {new Activiti(500, httpConfig)});

        File processDirectory = new File(Main.class.getClassLoader().getResource("processes").getPath());
        File[] processDefinitionFiles = processDirectory.listFiles();

        ExecutorService executor = Executors.newCachedThreadPool();

        AtomicInteger count = new AtomicInteger(0);
        (Arrays.stream(processDefinitionFiles).parallel()).forEach(file -> {
            loadBalancer.deployDefinition(file);

            BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
            InputStreamProvider provider = new ActivitiSimuluator.FileInputStreamProvider(file);
            BpmnModel model = bpmnXMLConverter.convertToBpmnModel(provider, false, false);

            String logFileName = file.getName().substring(0, file.getName().indexOf('.'));
            System.out.println(logFileName);
            File logFile = new File(Main.class.getClassLoader().getResource("logs/" + logFileName + ".mxml").getPath());

            List<ProcessInstance> instanceList = new LogExtractor().extractProcessInstance(logFile);

            instanceList.forEach(processInstance -> {
                SimulatableProcessInstance instance = (SimulatableProcessInstance) processInstance;
                instance.setTrace(new ActivitiUtil().buildTrace(model, processInstance));
            });

            for(int k = 0; k < 2; k++) {
                for (int i = 0; i < instanceList.size() - 1; i++) {
                    final int pos = i;
                    executor.submit(() -> {
                        loadBalancer.launchProcessInstance(instanceList.get(pos));
                        System.out.println(count.getAndIncrement());
                    });
//                try {
//                    TimeUnit.MILLISECONDS.sleep(instanceList.get(i + 1).getTasks().get(0).start - instanceList.get(i).getTasks().get(0).start);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
                }
            }
        });



    }
    static File[] multiply(File[] origin, int efficient) {
        File[] result = new File[origin.length * efficient];

        for(int i = 0; i < result.length; i++) {
            result[i] = origin[i % origin.length];
        }

        return result;
    }
}
