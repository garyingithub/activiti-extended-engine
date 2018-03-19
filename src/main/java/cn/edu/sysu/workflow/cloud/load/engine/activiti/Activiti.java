package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import cn.edu.sysu.workflow.cloud.load.TimeFollower;
import cn.edu.sysu.workflow.cloud.load.data.SimulatableProcessInstance;
import cn.edu.sysu.workflow.cloud.load.data.TraceNode;
import cn.edu.sysu.workflow.cloud.load.engine.Server;
import cn.edu.sysu.workflow.cloud.load.engine.WorkflowEngine;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.http.HttpHelper;
import cn.edu.sysu.workflow.cloud.load.http.HttpHelperSelector;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Activiti implements WorkflowEngine, TimeFollower {

    protected Server server;

    private final Map<String, String> headers;

    public Activiti(int capacity, HttpConfig httpConfig) {
        this.httpConfig = httpConfig;
        this.server = new Server(capacity);
        this.headers = new HashMap<>();
        String base64Code = "Basic " + Base64Utils.encodeToString("admin:admin".getBytes());
        headers.put("Authorization", base64Code);
    }

    @Override
    public void generateWorkload(SimulatableProcessInstance processInstance) {
        String url = ActivitiUtil.INSTANCE.buildStartProcessUrl(httpConfig, processInstance);
        String instanceId = HttpHelperSelector.SELECTOR.select().
                postObject(url,
                        new HashMap<>(), headers);

        executeTrace(instanceId, processInstance.getTrace());
//        new cn.edu.sysu.workflow.cloud.load.engine.activiti.Activiti(3, httpConfig).simulateProcessInstance(processInstance);
        server.deployWorkload(processInstance.getFrequencyList());
    }

    @Override
    public void deployDefinition(File file) {
        HttpHelperSelector.SELECTOR.select().postFile(ActivitiUtil.INSTANCE.buildDeployDefinitionUrl(httpConfig), file, headers);
    }

    @Override
    public void pastPeriod() {
        server.pastPeriod();
    }


    private HttpConfig httpConfig;

    private void executeTrace(String processId, TraceNode root) {
        if (root.getTask() == null) {
            for (TraceNode node : root.getNextNodes()) {
                claimTask(processId, node);
            }
        }
    }

    private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    private void claimTask(String instanceId, TraceNode node) {
        String url = ActivitiUtil.INSTANCE.buildClaimUrl(httpConfig, instanceId, node.getTask().getTaskName());
        HttpHelper helper = HttpHelperSelector.SELECTOR.select();
        helper.asyncPostObject(url, new HashMap<>(), headers, (call, response) ->
                scheduledExecutorService.schedule(() -> completeTask(instanceId, node),
                        (node.getTask().getEnd() - node.getTask().start),
                        TimeUnit.NANOSECONDS));
    }

    private void completeTask(String instanceId, TraceNode node) {
        String url = ActivitiUtil.INSTANCE.buildCompleteUrl(httpConfig, instanceId, node.getTask().getTaskName());
        HttpHelper helper = HttpHelperSelector.SELECTOR.select();
        helper.asyncPostObject(url, node.getVariables(), headers,
                (call, response) -> {
                    boolean processFinished;

                    try {
                        String finishedString = response.body().string();
                        processFinished = Boolean.valueOf(finishedString);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (processFinished) {
                        node.getNextNodes().forEach(traceNode ->
                                scheduledExecutorService.schedule(() ->
                                        claimTask(instanceId, traceNode),
                                        traceNode.getTask().getStart() - node.getTask().getEnd(),
                                        TimeUnit.NANOSECONDS));
                    } else {
                        System.out.println("process finishes");
                    }
                });
    }


}
