package cn.edu.sysu.workflow.cloud.load.process.activiti;

import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.http.HttpHelper;
import cn.edu.sysu.workflow.cloud.load.http.async.AsyncHttpHelper;
import org.springframework.util.Base64Utils;
import cn.edu.sysu.workflow.cloud.load.process.ProcessEngine;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Activiti implements ProcessEngine {

    private final String EXTENDED_PREFIX = "/extended";

    private Map<String, String> headers;
    private HttpHelper httpHelper;
    private AsyncHttpHelper asyncHttpHelper;

    public Activiti(HttpConfig httpConfig) {
        this.headers = new HashMap<>();
        String base64Code = "Basic " + Base64Utils.encodeToString("admin:admin".getBytes());
        headers.put("Authorization", base64Code);
        httpHelper = new HttpHelper(httpConfig);
        asyncHttpHelper = new AsyncHttpHelper(httpConfig);
    }

    @Override
    public String startProcess(String definitionId, Object data) {
        String url = EXTENDED_PREFIX.concat("/startProcess/").concat(definitionId);
        return this.httpHelper.postObject(url, data, headers);
    }

    @Override
    public String startTask(String processId, String taskName) {
        String url;
        try {
            url = EXTENDED_PREFIX.concat("/claimTask/").concat(processId).concat("/").concat(URLEncoder.encode(taskName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return this.asyncHttpHelper.postObject(url, "", headers);
    }

    @Override
    public void asyncStartTask(String processId, String taskName) {

    }

    @Override
    public String completeTask(String processId, String taskName, Map<String, Object> variables) {
        String url;
        try {
            url = EXTENDED_PREFIX.concat("/completeTask/").concat(processId).concat("/").concat(URLEncoder.encode(taskName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return this.asyncHttpHelper.postObject(url, variables, headers);
    }

    @Override
    public String addProcessDefinition(String name, File file) {
        final String url = "/repository/deployments";
        return httpHelper.postFile(url, file, headers);
    }
}
