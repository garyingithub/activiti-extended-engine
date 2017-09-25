package cn.edu.sysu.workflow.cloud.load;

import cn.edu.sysu.workflow.cloud.load.engine.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
@ComponentScan(basePackages = "cn.edu.sysu.workflow.cloud.load")
public class LogSimulator {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(LogSimulator.class);

        SimulatorUtil simulatorUtil = applicationContext.getBean(SimulatorUtil.class);
        ActivitiUtil activitiUtil = applicationContext.getBean(ActivitiUtil.class);
        HttpConfig httpConfig = applicationContext.getBean(HttpConfig.class);

        File processDirectory = new File(Main.class.getClassLoader().getResource("processes").getPath());
        File[] processDefinitionFiles = processDirectory.listFiles();

        List<ActivitiSimuluator> activitiSimuluators = new ArrayList<>();

        Arrays.stream(processDefinitionFiles).forEach(file -> {
            String fileName = file.getName().substring(0, file.getName().indexOf('.'));
            File logFile = new File(Main.class.getClassLoader().getResource("logs/" + fileName + ".mxml").getPath());

            for (int i = 0; i < 5; i++) {
                activitiSimuluators.add(new ActivitiSimuluator(file, logFile, httpConfig, activitiUtil, simulatorUtil));
            }
        });


//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
//        for(int i = 0; i < activitiSimuluators.size(); i++) {
//            final int pos = i;
//            executor.execute(() -> activitiSimuluators.get(pos).simulate());
//        }
        while (true) {
            activitiSimuluators.parallelStream().forEach(ActivitiSimuluator::simulate);
            try {
                TimeUnit.MINUTES.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
//        ActivitiSimuluator activitiSimuluator = applicationContext.getBean(ActivitiSimuluator.class);
//        activitiSimuluator.simulate();
//        while (true) {
//            Scanner scanner = new Scanner(System.in);
//
//            ActivitiSimuluator.rate = scanner.nextInt();
//        }
    }

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    public HttpConfig httpConfig() {
        // TODO 可在配置文件中配置
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setHost("stack");
        httpConfig.setPort("8081");
        return httpConfig;
    }

//    @Bean
//    public ActivitiSimuluator activitiSimuluator(HttpConfig httpConfig, ActivitiUtil activitiUtil, SimulatorUtil simulatorUtil) {
//        File logFile = new File(getClass().getClassLoader().getResource("logs/simulation_logs.mxml").getPath());
//        logger.info("load log {}", logFile.getName());
//        File definitionFIle = new File(getClass().getClassLoader().getResource("processes/parallel.bpmn.xml").getPath());
//        logger.info("load bpmn {}", definitionFIle.getName());
//        return new ActivitiSimuluator(definitionFIle, logFile, httpConfig, activitiUtil, simulatorUtil);
//    }
}
