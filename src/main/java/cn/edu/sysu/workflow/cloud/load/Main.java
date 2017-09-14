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
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


//@Configuration
//@ComponentScan(basePackages = {"process","com.xueyou.demo"})
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
//        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(this.getClass());
        HttpConfig activitiConfig = new HttpConfig();
        activitiConfig.setHost("localhost");
        activitiConfig.setPort("8081");

        Activiti activiti = new Activiti(activitiConfig);

        Executor executor = Executors.newCachedThreadPool();

        List<String> instanceIdList = new ArrayList<>();

        List<Executor> executorList = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            instanceIdList.add(activiti.startProcess("testUserTasks", null));
            executorList.add(Executors.newSingleThreadExecutor());
        }



        long times = 0;


        try {
            executor.execute(() -> {
                while (true) {
                    Scanner scanner = new Scanner(System.in);
                    rate = scanner.nextInt();
                }
            });
            while (true) {
                final int pos = new Long(times % instanceIdList.size()).intValue();
                executorList.get(pos).execute(() -> runTask(instanceIdList.get(pos), activiti));

                times++;
                logger.debug(String.valueOf(times));
                TimeUnit.MILLISECONDS.sleep(1000 / rate);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
