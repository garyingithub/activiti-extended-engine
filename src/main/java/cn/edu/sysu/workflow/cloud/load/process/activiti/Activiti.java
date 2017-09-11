package cn.edu.sysu.workflow.cloud.load.process.activiti;

import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.http.HttpHelper;
import org.springframework.util.Base64Utils;
import cn.edu.sysu.workflow.cloud.load.process.ProcessEngine;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Activiti implements ProcessEngine {

    private String base64Code = "Basic " + Base64Utils.encodeToString("admin:admin".getBytes());
    private final String URL_PREFIX = "/activiti-rest";
    private final String REPOSITORY_PREFIX = "/repository";
    private final String RUNTIME_PREFIX = "/runtime";
    private final String EXTENDED_PREFIX = "/extended";
    private HttpConfig httpConfig;

    private Map<String, String> headers;
    private HttpHelper httpHelper;

    public Activiti(HttpConfig httpConfig) {
        this.httpConfig = httpConfig;
        this.headers = new HashMap<>();
        headers.put("Authorization", this.base64Code);
        httpHelper = new HttpHelper(httpConfig);
    }

    @Override
    public String startProcess(String definitionId, Object data) {
        String url = EXTENDED_PREFIX.concat("/startProcess/").concat(definitionId);
        return this.httpHelper.postObject(url, data, headers);
    }

    @Override
    public String startTask(String processId, String taskName) {
        String url = null;
        try {
            url = EXTENDED_PREFIX.concat("/claimTask/").concat(processId).concat("/").concat(URLEncoder.encode(taskName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        ;
        return this.httpHelper.postObject(url, "", headers);
    }

    @Override
    public String completeTask(String processId, String taskName) {
        String url = null;
        try {
            url = EXTENDED_PREFIX.concat("/completeTask/").concat(processId).concat("/").concat(URLEncoder.encode(taskName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return this.httpHelper.postObject(url, "", headers);
    }

    @Override
    public String addProcessDefinition(String name, String location) {
//        final String url = "/repository/deployments";
//        ClassLoader classLoader = getClass().getClassLoader();
//        URL fileUrl = classLoader.getResource(location);
//        if (fileUrl == null) {
//            throw new RuntimeException("File doesn't exist");
//        }
//        File file = new File(fileUrl.getFile());
//        headers.put("Content-Disposition", "\"form-data;name=bpmn;filename=\" + name");
//        headers.put("Accept","text/xml,application/json,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
////        headers.put("Content-Type", "multipart/form-data");
//        return httpHelper.postForm(url, file, name, headers);
        return null;

    }
}
