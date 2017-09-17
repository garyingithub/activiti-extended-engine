package cn.edu.sysu.workflow.cloud.load;

import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.process.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.simulator.ActivitiSimuluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.lang.Thread.sleep;


@Configuration
@ComponentScan(basePackages = {"cn.edu.sysu.workflow.cloud.load"})
public class Main {
    static Logger logger = LoggerFactory.getLogger(Main.class);

    static void runTask(String instanceId, Activiti activiti) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("continue", false);

        activiti.completeTask(instanceId, "testUserTask0", variables);

    }

    static void completeProcessInstance(String instanceId, Activiti activiti) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("continue", true);
        activiti.completeTask(instanceId, "testUserTask0", variables);
        for (int j = 1; j < 4; j++) {
            activiti.completeTask(instanceId, "testUserTask" + j, variables);
        }
    }

    static volatile int rate = 1;
    public static void main(String[] args) {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(Main.class);
        SimulatorUtil simulatorUtil = applicationContext.getBean(SimulatorUtil.class);

        //TODO 可在application.properties中配置
        HttpConfig activitiConfig = new HttpConfig();
        activitiConfig.setHost("tencent");
        activitiConfig.setPort("8081");

        Activiti activiti = new Activiti(activitiConfig);

        // 上传processes文件夹中的流程文件
        simulatorUtil.scanAndUploadDefinitions(activiti);

        List<String> instanceIdList = new ArrayList<>();

        List<Executor> executorList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            instanceIdList.add(activiti.startProcess("testUserTasks", null));
            executorList.add(Executors.newSingleThreadExecutor());
//            executorList.add(new SimulatorUtil.QueueAwareThreadExecutor());
        }

        long times = 0;
        try {
            System.out.println("Please input request rate, default value is " + rate + "/s");
            new Thread(() -> {
                while (true) {
                    Scanner scanner = new Scanner(System.in);
                    rate = scanner.nextInt();
                    System.out.println("Request rate has been modified as " + rate + "/s");
                }
            }).start();
            while (true) {
                long start = System.currentTimeMillis();
                final int pos = new Long(times % instanceIdList.size()).intValue();
                executorList.get(pos).execute(() -> runTask(instanceIdList.get(pos), activiti));

                times++;
                logger.debug(String.valueOf(times));
                long period = new Double(1000.0 * 1000.0 / rate).longValue() - (System.currentTimeMillis() - start);
                TimeUnit.MICROSECONDS.sleep(period > 0 ? period : 0);
//                System.out.println((System.currentTimeMillis() - start));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
