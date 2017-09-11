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
        activitiConfig.setPort("8080");

        final String url = "simulation_logs.mxml";
        ClassLoader classLoader = Main.class.getClassLoader();
        URL fileUrl = classLoader.getResource(url);
        if (fileUrl == null) {
            throw new RuntimeException("File doesn't exist");
        }
        File file = new File(fileUrl.getFile());
        ActivitiSimuluator activitiSimuluator = new ActivitiSimuluator(file, activitiConfig);
    }
}
