package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import cn.edu.sysu.workflow.cloud.load.engine.HttpConfig;
import org.junit.Before;
import org.junit.Test;

public class ActivitiTest {

    private Activiti activiti;

    String instanceId;

    @Before
    public void init() {
        HttpConfig config = new HttpConfig();
        config.setHost("localhost");
        config.setPort("8081");

        Activiti activiti = new Activiti(config);
        this.activiti = activiti;

        instanceId = activiti.startProcess("testUserTasksWithParallel", null);
    }

    @Test
    public void startProcess() throws Exception {
        activiti.startProcess("testUserTasks", null);
    }

    @Test
    public void startTask() throws Exception {
        activiti.claimTask(instanceId, "testUserTask");
    }

    @Test
    public void completeTask() throws Exception {
//        while (Boolean.TRUE.toString().equals(activiti.completeTask(instanceId, "testUserTask", null)));
    }

    @Test
    public void addProcessDefinition() throws Exception {
        String location = "test-engine.xml";
//        activiti.deployProcessDefinition("test-engine", location);
    }

}