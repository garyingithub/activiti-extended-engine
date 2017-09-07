package process.activiti;

import http.HttpConfig;
import http.HttpHelper;

import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import process.ProcessEngine;

import java.util.HashMap;
import java.util.Map;

public class Activiti implements ProcessEngine {

    private String base64Code = "Basic " + Base64Utils.encodeToString("admin:admin".getBytes());
    private final String URL_PREFIX = "/activiti-rest";
    private final String REPOSITORY_PREFIX = "/repository";
    private final String RUNTIME_PREFIX = "/runtime";
    private HttpConfig httpConfig;

    private Map<String, String> headers;
    private HttpHelper httpHelper;

    public Activiti(HttpConfig httpConfig) {
        this.httpConfig = httpConfig;
        this.headers = new HashMap<>();
        // headers.put("BASE_64_CODE", base64Code);
        headers.put("Cookies",  "optimizelyEndUserId=oeu1504498868195r0.2692424258474624; _ga=GA1.1.799790894.1504498871; ACTIVITI_REMEMBER_ME=ZWJDbUxGUXNwWjIwK3pldEI3dHYyUT09Ong4ODhiemtMaGJCb3NhMXdFMjNjb2c9PQ; JSESSIONID=A7F6F1A60046E90A699C38E24FED9F76\n");
        httpHelper = new HttpHelper(httpConfig);
    }

    @Override
    public <ProcessDefinition> String startProcess(ProcessDefinition processDefinition) {
        String url = this.URL_PREFIX.concat(this.RUNTIME_PREFIX).concat("process-instances");
        String instanceId = this.httpHelper.postObject(url, processDefinition, headers);
        return instanceId;
    }

    @Override
    public String startTask(String processId, String taskName) {
        return null;
    }

    @Override
    public String completeTask(String processId, String taskName) {
        return null;
    }

    @Override
    public <ProcessDefinition> String addProcessDefinition(String processDefinitionId, ProcessDefinition paramObject) {
     return null;
    }
}
