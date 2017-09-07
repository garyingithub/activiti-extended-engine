package process.activiti;

import http.HttpConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ActivitiTest {

    private Activiti activiti;

    @Before
    public  void init() {
        HttpConfig config = new HttpConfig();
        config.setHost("localhost");
        config.setPort("8080");

        Activiti activiti = new Activiti(config);
      this.activiti = activiti;
    }

    @Test
    public void startProcess() throws Exception {
        ProcessInstanceCreateRequest processDefinition = new ProcessDefinition();
        processDefinition.setProcessDefinitionId("hireProcessWithJpa:1:7");
        processDefinition.setBusinessKey("hireProcessWithJpa");

        activiti.startProcess(processDefinition);
    }

    @Test
    public void startTask() throws Exception {
    }

    @Test
    public void completeTask() throws Exception {
    }

    @Test
    public void addProcessDefinition() throws Exception {
    }

}