package cn.edu.sysu.workflow.cloud.load.process.activiti;

import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ActivitiTest {

    private Activiti activiti;

    @Before
    public void init() {
        HttpConfig config = new HttpConfig();
        config.setHost("localhost");
        config.setPort("8080");

        Activiti activiti = new Activiti(config);
        this.activiti = activiti;
    }

    @Test
    public void startProcess() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("name", "gary");
        data.put("email", "s");
        data.put("phoneNumber", "111");
        activiti.startProcess("hireProcessWithJpa", data);
    }

    @Test
    public void startTask() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("name", "gary");
        data.put("email", "s");
        data.put("phoneNumber", "111");
        String instanceId = activiti.startProcess("hireProcessWithJpa", data);
        activiti.startTask(instanceId, "Telephone interview");
    }

    @Test
    public void completeTask() throws Exception {
    }

    @Test
    public void addProcessDefinition() throws Exception {
        String location = "test-process.xml";
        activiti.addProcessDefinition("test-process", location);
    }

}