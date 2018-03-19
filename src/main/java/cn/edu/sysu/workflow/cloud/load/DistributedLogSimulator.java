package cn.edu.sysu.workflow.cloud.load;

import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.DistributedActiviti;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.simulator.SimulatorUtil;
import cn.edu.sysu.workflow.cloud.load.simulator.activiti.ActivitiSimuluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@ComponentScan(basePackages = "cn.edu.sysu.workflow.cloud.load")
public class DistributedLogSimulator {

    static String host = "222.200.180.59";
//    static String host = "127.0.0.1";

    public static String[] ports = new String[]{"8081", "8082", "8083", "8084", "8085"};

    public static int capacity = 1000;
    public static int approach = 2;
    public static void main(String[] args) {

        if(args.length > 0) {
            host = args[0];
        }

        if(args.length > 1) {
            ports = args[1].split(",");
        }

        if(args.length > 2) {
            capacity = Integer.parseInt(args[2]);
        }

        if(args.length > 3) {
            approach = Integer.parseInt(args[3]);
        }

        String rootDir = "/Users/mac/IdeaProjects/load-simulator/target/classes/";
//        String rootDir = "/";

        if(args.length > 4) {
            rootDir = args[4];
        }


        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(DistributedLogSimulator.class);

        SimulatorUtil simulatorUtil = new SimulatorUtil();
        ActivitiUtil activitiUtil = applicationContext.getBean(ActivitiUtil.class);
        HttpConfig httpConfig = applicationContext.getBean(HttpConfig.class);

        String processDir = rootDir + "processes";
        File processDirectory = new File(processDir);

        File[] processDefinitionFiles = processDirectory.listFiles();

        List<ActivitiSimuluator> activitiSimuluators = new ArrayList<>();

        DistributedActiviti activiti = new DistributedActiviti(getHttpConfigs());
        for (File file : processDefinitionFiles) {
            String fileName = file.getName().substring(0, file.getName().indexOf('.'));
            File logFile = new File(rootDir + ("logs/" + fileName + ".mxml"));

            for (int i = 0; i < 5; i++) {
                activitiSimuluators.add(new ActivitiSimuluator(file, logFile, activiti, activitiUtil, simulatorUtil));
            }
        }


        final Random random = new Random();
        Executor executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 1; i++) {
            activitiSimuluators.parallelStream().forEach(activitiSimuluator -> executor.execute(activitiSimuluator::simulate));
//            activitiSimuluators.parallelStream().forEach(ActivitiSimuluator::simulate);
        }
        System.out.println("final workload is " + ActivitiSimuluator.count.get());
    }

    final Logger logger = LoggerFactory.getLogger(getClass());

    static List<HttpConfig> getHttpConfigs() {
        List<HttpConfig> configs = new ArrayList<>();
        for (int i = 0; i < ports.length; i++) {
            HttpConfig httpConfig = new HttpConfig();
//            httpConfig.setHost("119.29.61.136");

            httpConfig.setHost(host);
            httpConfig.setPort(ports[i]);
            configs.add(httpConfig);
        }
        return configs;
    }


}
