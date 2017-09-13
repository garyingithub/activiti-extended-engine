package cn.edu.sysu.workflow.cloud.load;

import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.process.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.simulator.ActivitiSimuluator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


//@Configuration
//@ComponentScan(basePackages = {"process","com.xueyou.demo"})
public class Main {
    public static void main(String[] args) {
//        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(this.getClass());
        HttpConfig activitiConfig = new HttpConfig();
        activitiConfig.setHost("localhost");
        activitiConfig.setPort("8081");

        Activiti activiti = new Activiti(activitiConfig);

        for(int i = 0; i < 100; i++) {
            String instanceId = activiti.startProcess("testUserTasksWithParallel", null);
            Map<String, Object> variables = new HashMap<>();
            variables.put("goUp", true);
            while (Boolean.TRUE.toString().equals(activiti.completeTask(instanceId, "testUserTask", variables)));
        }

    }
}
