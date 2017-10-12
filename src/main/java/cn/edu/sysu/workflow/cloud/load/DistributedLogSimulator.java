package cn.edu.sysu.workflow.cloud.load;

import cn.edu.sysu.workflow.cloud.load.engine.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.DistributedActiviti;
import cn.edu.sysu.workflow.cloud.load.simulator.SimulatorUtil;
import cn.edu.sysu.workflow.cloud.load.simulator.activiti.ActivitiSimuluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Configuration
@ComponentScan(basePackages = "cn.edu.sysu.workflow.cloud.load")
public class DistributedLogSimulator {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(DistributedLogSimulator.class);

        SimulatorUtil simulatorUtil = new SimulatorUtil();
        ActivitiUtil activitiUtil = applicationContext.getBean(ActivitiUtil.class);
        HttpConfig httpConfig = applicationContext.getBean(HttpConfig.class);

        File processDirectory = new File(Main.class.getClassLoader().getResource("processes").getPath());
        File[] processDefinitionFiles = processDirectory.listFiles();

        List<ActivitiSimuluator> activitiSimuluators = new ArrayList<>();

        DistributedActiviti activiti = new DistributedActiviti(getHttpConfigs());
        Arrays.stream(processDefinitionFiles).forEach(file -> {
            String fileName = file.getName().substring(0, file.getName().indexOf('.'));
            File logFile = new File(Main.class.getClassLoader().getResource("logs/" + fileName + ".mxml").getPath());

            for (int i = 0; i < 1; i++) {
                activitiSimuluators.add(new ActivitiSimuluator(file, logFile, activiti, activitiUtil, simulatorUtil));
//                activitiSimuluators.forEach(activitiSimuluator -> activitiSimuluator.setSimulatorUtil(simulatorUtil));
            }
        });

        final Random random = new Random();
        Executor executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 1; i++) {
            activitiSimuluators.forEach(activitiSimuluator -> executor.execute(activitiSimuluator::simulate));
//            activitiSimuluators.parallelStream().forEach(ActivitiSimuluator::simulate);
//            activitiSimuluators.get(0).simulate();
            try {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(300));

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    final Logger logger = LoggerFactory.getLogger(getClass());

    static List<HttpConfig> getHttpConfigs() {
        List<HttpConfig> configs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            HttpConfig httpConfig = new HttpConfig();
            httpConfig.setHost("stack");
            httpConfig.setPort(String.valueOf(8091 + i));
            configs.add(httpConfig);
        }
        return configs;
    }


}
