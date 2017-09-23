package cn.edu.sysu.workflow.cloud.load.simulator.activiti;

import cn.edu.sysu.workflow.cloud.load.engine.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.ActivitiUtil;
import cn.edu.sysu.workflow.cloud.load.simulator.SimulatorUtil;
import org.junit.Test;

import java.io.File;
import java.util.Scanner;

public class ActivitiSimuluatorTest {

    @Test
    public void simulate() throws Exception {
        File definitionFIle = new File(ActivitiSimuluator.class.getClassLoader().getResource("processes/parallel.bpmn.xml").getPath());
        File logFile = new File(ActivitiSimuluator.class.getClassLoader().getResource("logs/simulation_logs.mxml").getPath());
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setHost("tencent");
        httpConfig.setPort("8081");

        ActivitiSimuluator activitiSimuluator = new ActivitiSimuluator(definitionFIle, logFile, httpConfig, new ActivitiUtil(), new SimulatorUtil());
        activitiSimuluator.simulate();

        while (true) {
            Scanner scanner = new Scanner(System.in);
            scanner.nextInt();
        }

    }

}