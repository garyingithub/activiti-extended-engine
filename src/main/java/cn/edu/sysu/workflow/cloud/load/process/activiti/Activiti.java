package cn.edu.sysu.workflow.cloud.load.process.activiti;

import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.http.HttpHelper;
import cn.edu.sysu.workflow.cloud.load.http.async.OkHttpCallback;
import okhttp3.Call;
import okhttp3.Response;
import org.springframework.util.Base64Utils;
import cn.edu.sysu.workflow.cloud.load.process.ProcessEngine;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Activiti implements ProcessEngine {

    private final String EXTENDED_PREFIX = "/extended";

    private Map<String, String> headers;
    private HttpHelper httpHelper;

    private HttpConfig httpConfig;
    private Timer timer = new Timer();

    public Activiti(HttpConfig httpConfig) {
        this.headers = new HashMap<>();
        String base64Code = "Basic " + Base64Utils.encodeToString("admin:admin".getBytes());
        headers.put("Authorization", base64Code);
        httpHelper = new HttpHelper();
        this.httpConfig = httpConfig;
    }

    private String buildUrl(String url) {
        return httpConfig.getAddress().concat(url);
    }
    @Override
    public String startProcess(String definitionId, Object data) {
        String url = EXTENDED_PREFIX.concat("/startProcess/").concat(definitionId);
        return this.httpHelper.postObject(buildUrl(url), data, headers);
    }

    @Override
    public String startTask(String processId, String taskName) {
        String url;
        try {
            url = EXTENDED_PREFIX.concat("/claimTask/").concat(processId).concat("/").concat(URLEncoder.encode(taskName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return this.httpHelper.postObject(buildUrl(url), "", headers);
    }

    class ActivitiStartTaskCallback implements OkHttpCallback {

        private long needTime;
        private String processId;
        private String taskName;
        private Map<String, Object> variables;

        public ActivitiStartTaskCallback(long needTime, String processId, String taskName, Map<String, Object> variables) {
            this.needTime = needTime;
            this.processId = processId;
            this.taskName = taskName;
            this.variables = variables;
        }

        class CompleteTaskTimerTask extends TimerTask {

            @Override
            public void run() {
                asyncCompleteTask(processId, taskName, variables);
            }
        }

        @Override
        public void call(Call call, Response response) {
            timer.schedule(new CompleteTaskTimerTask(), needTime);
        }
    }

    @Override
    public void executeTask(String processId, String taskName, long needTime, Map<String, Object> variables) {
        String url;
        try {
            url = EXTENDED_PREFIX.concat("/claimTask/").concat(processId).concat("/").concat(URLEncoder.encode(taskName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        this.httpHelper.asyncPostObject(buildUrl(url), "", headers, new ActivitiStartTaskCallback(needTime, processId, taskName, variables));
    }

    @Override
    public String completeTask(String processId, String taskName, Map<String, Object> variables) {
        String url;
        try {
            url = EXTENDED_PREFIX.concat("/completeTask/").concat(processId).concat("/").concat(URLEncoder.encode(taskName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return this.httpHelper.postObject(buildUrl(url), variables, headers);
    }

    @Override
    public void asyncCompleteTask(String processId, String taskName, Map<String, Object> variables) {
        String url;
        try {
            url = EXTENDED_PREFIX.concat("/completeTask/").concat(processId).concat("/").concat(URLEncoder.encode(taskName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        this.httpHelper.asyncPostObject(buildUrl(url), variables, headers, new OkHttpCallback() {
            @Override
            public void call(Call call, Response response) {

            }
        });
    }

    @Override
    public String addProcessDefinition(String name, File file) {
        final String url = "/repository/deployments";
        return httpHelper.postFile(url, file, headers);
    }
}
