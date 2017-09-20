package cn.edu.sysu.workflow.cloud.load.simulator.activiti;

import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.process.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.process.activiti.ActivitiUtil;
import org.junit.Test;

import java.io.File;
import java.util.Scanner;

import static org.junit.Assert.*;

public class ActivitiSimuluatorTest {

    @Test
    public void simulate() throws Exception {
        File definitionFIle = new File(ActivitiSimuluator.class.getClassLoader().getResource("processes/testUserTasks-bimp.bpmn20.xml").getPath());
        File logFile = new File(ActivitiSimuluator.class.getClassLoader().getResource("logs/simulation_logs.mxml").getPath());
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setHost("localhost");
        httpConfig.setPort("8081");

        ActivitiSimuluator activitiSimuluator = new ActivitiSimuluator(definitionFIle, logFile, httpConfig, new ActivitiUtil());
        activitiSimuluator.simulate();

        while (true) {
            Scanner scanner = new Scanner(System.in);
            scanner.nextInt();
        }

    }

}