package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import cn.edu.sysu.workflow.cloud.load.engine.ProcessEngine;
import cn.edu.sysu.workflow.cloud.load.engine.Server;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.http.HttpHelper;
import cn.edu.sysu.workflow.cloud.load.http.async.OkHttpCallback;
import cn.edu.sysu.workflow.cloud.load.simulator.activiti.ActivitiSimuluator;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.SimulatableProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.TraceNode;
import okhttp3.Call;
import okhttp3.Response;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class Activiti extends Server implements ProcessEngine {

    private final String EXTENDED_PREFIX = "/extended";

    private Map<String, String> headers;
    private HttpHelper httpHelper;

    //    private Logger logger = LoggerFactory.getLogger(getClass());
    private HttpConfig httpConfig;

    public static AtomicLong processedCount = new AtomicLong(0);
    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    public Activiti(int id, HttpConfig httpConfig) {
        super(id);
        this.httpConfig = httpConfig;
        this.headers = new HashMap<>();
        String base64Code = "Basic " + Base64Utils.encodeToString("admin:admin".getBytes());
        headers.put("Authorization", base64Code);
        httpHelper = new HttpHelper();
    }

    private String buildUrl(String url) {
        return httpConfig.getAddress().concat(url);
    }


    @Override
    public void startProcess(ProcessInstance processInstance, Object data, StringCallback callback) {
        String url = EXTENDED_PREFIX.concat("/startProcess/").concat(processInstance.getDefinitionId());
        callback.call(this.httpHelper.postObject(buildUrl(url), data, headers));
        addLoad(processInstance.getFrequencyList());
    }

    @Override
    public void claimTask(String processId, String taskName, StringCallback callback) {
        String url = EXTENDED_PREFIX.concat("/claimTask")
                .concat(encodePathVariable(processId))
                .concat(encodePathVariable(taskName));
        callback.call(this.httpHelper.postObject(buildUrl(url), "", headers));
    }

    @Override
    public void completeTask(String processId, String taskName, StringCallback callback) {
        String url = EXTENDED_PREFIX.concat("/completeTask")
                .concat(encodePathVariable(processId))
                .concat(encodePathVariable(taskName));
        callback.call(this.httpHelper.postObject(buildUrl(url), "", headers));
    }

    @Override
    public void deployProcessDefinition(String name, File file, StringCallback callback) {
        final String url = "/repository/deployments";
        callback.call(httpHelper.postFile(buildUrl(url), file, headers));
    }

    @Override
    public void simulateProcessInstance(SimulatableProcessInstance processInstance) {
        startProcess(processInstance, null, result -> executeTrace(result, processInstance.getTrace()));
        processInstance.getFrequencyList().forEach(integer -> processedCount.addAndGet(integer));
    }


    private void executeTrace(String processId, TraceNode root) {
        if (root.getTask() == null) {
            for (TraceNode node : root.getNextNodes()) {
                asyncClaimTask(processId, node);
            }
        }
    }

    class AfterClaimTaskCallback implements OkHttpCallback {

        private String processId;
        private TraceNode current;

        @Override
        public void call(Call call, Response response) {
            scheduledExecutorService.schedule(() -> asyncCompleteTask(processId, current), current.getTask().getDuration(), TimeUnit.MILLISECONDS);
        }

        AfterClaimTaskCallback(String processId, TraceNode current) {
            this.processId = processId;
            this.current = current;
        }
    }

    private void asyncClaimTask(String processId, TraceNode root) {
        String url = EXTENDED_PREFIX.concat("/claimTask")
                .concat(encodePathVariable(processId))
                .concat(encodePathVariable(root.getTask().getTaskName()));

        this.httpHelper.asyncPostObject(buildUrl(url), "", headers, new AfterClaimTaskCallback(processId, root));
    }

    private void asyncCompleteTask(String processId, TraceNode root) {
        String url = EXTENDED_PREFIX.concat("/completeTask")
                .concat(encodePathVariable(processId))
                .concat(encodePathVariable(root.getTask().getTaskName()));
        httpHelper.asyncPostObject(buildUrl(url), root.getVariables(), headers, new AfterCompleteTaskCallback(processId, root));

    }

    class AfterCompleteTaskCallback implements OkHttpCallback {

        private String processId;
        private TraceNode currentNode;

        @Override
        public void call(Call call, Response response) {
            boolean processFinished;
            try {
                String finishedString = response.body().string();
                processFinished = Boolean.valueOf(finishedString);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (processFinished) {
                currentNode.getNextNodes().forEach(traceNode -> scheduledExecutorService.schedule(() -> asyncClaimTask(processId, traceNode), traceNode.getTask().getStart() - currentNode.getTask().getEnd(), TimeUnit.MILLISECONDS));
            }
        }

        AfterCompleteTaskCallback(String processId, TraceNode currentNode) {
            this.processId = processId;
            this.currentNode = currentNode;
        }
    }


    private String encodePathVariable(String pathVariable) {
        try {
            return "/".concat(URLEncoder.encode(pathVariable, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
