package cn.edu.sysu.workflow.cloud.load.simulator;

import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.process.activiti.Activiti;

import java.io.File;

public class ActivitiSimuluator extends Simulator {

    public ActivitiSimuluator(File file, HttpConfig httpConfig) {
        super(file);
        this.setEngine(new Activiti(httpConfig));
    }
    public ActivitiSimuluator(File file) {
        super(file);
    }
}
