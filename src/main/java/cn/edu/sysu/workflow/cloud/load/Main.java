package cn.edu.sysu.workflow.cloud.load;

import cn.edu.sysu.workflow.cloud.load.engine.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.simulator.SimulatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;


@Configuration
@ComponentScan(basePackages = {"cn.edu.sysu.workflow.cloud.load"})
public class Main {
    static Logger logger = LoggerFactory.getLogger(Main.class);

    static void runTask(String instanceId, Activiti activiti) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("continue", false);

//        activiti.completeTask(instanceId, "testUserTask0", variables);

    }


    static class Simulator implements Runnable {

        private long times;
        private Activiti activiti;
        private List<Executor> executorList;
        private List<String> instanceIdList;

        public Simulator(long times, Activiti activiti, List<Executor> executorList, List<String> instanceIdList) {
            this.times = times;
            this.activiti = activiti;
            this.executorList = executorList;
            this.instanceIdList = instanceIdList;
        }

        @Override
        public void run() {
            try {
                int rate = 10;
                while (true) {
                    long start = System.nanoTime();
                    final int pos = new Long(times % instanceIdList.size()).intValue();
                    executorList.get(pos).execute(() -> runTask(instanceIdList.get(pos), activiti));

                    times++;
                    long period = new Double(Math.pow(10, 9) / rate).longValue() - (System.nanoTime() - start);
                    System.out.println(String.valueOf(((System.nanoTime() - start) / 1000)));
                    if (period < 0) {
//                    throw new RuntimeException("It can't be any faster");
                        System.out.println("---");
                        period = 0;
                    }

                    TimeUnit.NANOSECONDS.sleep(period > 0 ? period : 0);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static volatile int rate = 250;

    public static void main(String[] args) {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(Main.class);
        SimulatorUtil simulatorUtil = applicationContext.getBean(SimulatorUtil.class);

        //TODO 可在application.properties中配置
        HttpConfig activitiConfig = new HttpConfig();
        activitiConfig.setHost("tencent");
        activitiConfig.setPort("8081");

        Activiti activiti = new Activiti(1, activitiConfig);

        // 上传processes文件夹中的流程文件
        simulatorUtil.scanAndUploadDefinitions(activiti);

        List<String> instanceIdList = new ArrayList<>();

        List<Executor> executorList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
//            instanceIdList.add(activiti.startProcess("testUserTasks", null));
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
            Executor simulatorExecutor = Executors.newFixedThreadPool(10);
            for (int i = 0; i < 10; i++) {
                simulatorExecutor.execute(new Simulator(times, activiti, executorList, instanceIdList));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
