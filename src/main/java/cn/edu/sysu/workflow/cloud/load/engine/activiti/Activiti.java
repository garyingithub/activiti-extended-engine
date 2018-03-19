package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import cn.edu.sysu.workflow.cloud.load.engine.ProcessEngine;
import cn.edu.sysu.workflow.cloud.load.engine.Server;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.http.HttpHelper;
import cn.edu.sysu.workflow.cloud.load.http.async.OkHttpCallback;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.SimulatableProcessInstance;
import cn.edu.sysu.workflow.cloud.load.simulator.data.TraceNode;
import okhttp3.Call;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Activiti extends Server implements ProcessEngine {

    private final String EXTENDED_PREFIX = "/extended";

    private Map<String, String> headers;

    private final int[] flags = new int[]{2, 5};

    private final int[] weights = new int[]{2, 5, 10};
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
    }

    private HttpConfig getHttpConfig() {
        return httpConfig;
    }

    private static List<HttpHelper> helpers = new ArrayList<>();

    private static final int HTTP_CONNECTIONS = 1500;
    static {
        for (int i = 0; i < HTTP_CONNECTIONS; i++) {
            helpers.add(new HttpHelper());
        }
    }

    private static Random random = new Random();

    private static HttpHelper getHttpHelper() {
        return helpers.get(random.nextInt(helpers.size()));
    }

    private String buildUrl(String url) {
        return getHttpConfig().getAddress().concat(url);
    }


    @Override
    public void startProcess(ProcessInstance processInstance, Object data, StringCallback callback) {
        String url = EXTENDED_PREFIX.concat("/startProcess/").concat(processInstance.getDefinitionId());
        callback.call(getHttpHelper().postObject(buildUrl(url), data, headers));
//        addLoad(processInstance.getFrequencyList());
    }

    @Override
    public void claimTask(String processId, String taskName, StringCallback callback) {
        String url = EXTENDED_PREFIX.concat("/claimTask")
                .concat(encodePathVariable(processId))
                .concat(encodePathVariable(taskName));
        callback.call(getHttpHelper().postObject(buildUrl(url), "", headers));
    }

    @Override
    public void completeTask(String processId, String taskName, StringCallback callback) {
        String url = EXTENDED_PREFIX.concat("/completeTask")
                .concat(encodePathVariable(processId))
                .concat(encodePathVariable(taskName));
        callback.call(getHttpHelper().postObject(buildUrl(url), "", headers));
    }

    @Override
    public void deployProcessDefinition(String name, File file, StringCallback callback) {
        final String url = "/repository/deployments";
        callback.call(getHttpHelper().postFile(buildUrl(url), file, headers));
    }

    @Override
    public void simulateProcessInstance(SimulatableProcessInstance processInstance) {
        startProcess(processInstance, null, result -> executeTrace(result, processInstance.getTrace()));
//        processInstance.getFrequencyList().forEach(integer -> processedCount.addAndGet(integer));
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
//            logger.info("claim " + String.valueOf(claimCount.incrementAndGet()));

            scheduledExecutorService.schedule(() -> asyncCompleteTask(processId, current), (current.getTask().getEnd() - current.getTask().start), TimeUnit.MILLISECONDS);
        }

        AfterClaimTaskCallback(String processId, TraceNode current) {
            this.processId = processId;
            this.current = current;
        }
    }

    private int getWeight(String processId) {
        Integer id = Integer.parseInt(processId);
        return weights[id % flags.length];
    }

    private int getFlag(String processId) {
        Integer id = Integer.parseInt(processId);
        return flags[id % flags.length];
    }

    Logger logger = LoggerFactory.getLogger(getClass());

    public static AtomicLong claimCount = new AtomicLong(0);
    public static AtomicLong completeCount = new AtomicLong(0);

    private void asyncClaimTask(String processId, TraceNode root) {
        String url = EXTENDED_PREFIX.concat("/claimTask")//.concat(String.valueOf(getFlag(processId)))
                .concat(encodePathVariable(processId))
                .concat(encodePathVariable(root.getTask().getTaskName()));//.concat(encodePathVariable(String.valueOf(getWeight(processId))));
        getHttpHelper().asyncPostObject(buildUrl(url), new HashMap<>(), headers, new AfterClaimTaskCallback(processId, root));
    }

    private void asyncCompleteTask(String processId, TraceNode root) {
//        String url = EXTENDED_PREFIX.concat("/completeTask").concat(String.valueOf(getFlag(processId)))
        String url = EXTENDED_PREFIX.concat("/completeTask")
                .concat(encodePathVariable(processId))
                .concat(encodePathVariable(root.getTask().getTaskName()));//.concat(encodePathVariable(String.valueOf(getWeight(processId))));
        getHttpHelper().asyncPostObject(buildUrl(url), root.getVariables(), headers, new AfterCompleteTaskCallback(processId, root));

    }

    class AfterCompleteTaskCallback implements OkHttpCallback {

        private String processId;
        private TraceNode currentNode;

        @Override
        public void call(Call call, Response response) {
            boolean processFinished;
//            logger.info("complete " + String.valueOf(completeCount.incrementAndGet()));

            try {
                String finishedString = response.body().string();
                processFinished = Boolean.valueOf(finishedString);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (processFinished) {
                currentNode.getNextNodes().forEach(traceNode -> scheduledExecutorService.schedule(() -> asyncClaimTask(processId, traceNode), traceNode.getTask().getStart() - currentNode.getTask().getEnd(), TimeUnit.MILLISECONDS));
            } else {
                System.out.println("process finishes");
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
