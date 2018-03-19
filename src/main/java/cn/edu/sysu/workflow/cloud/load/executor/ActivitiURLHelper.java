package cn.edu.sysu.workflow.cloud.load.executor;

import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public enum ActivitiURLHelper {
    INSTANCE;

    private final String EXTENDED_PREFIX = "/extended";
    String buildClaimUrl(HttpConfig httpConfig, String instanceId, String taskName) {

        return String.format("%s%s%s%s%s",
                httpConfig.getAddress(),
                EXTENDED_PREFIX,
                "/claimTask",
                encodePathVariable(instanceId),
                encodePathVariable(taskName));
    }

    String buildCompleteUrl(HttpConfig httpConfig, String instanceId, String taskName) {

        return String.format("%s%s%s%s%s",
                httpConfig.getAddress(),
                EXTENDED_PREFIX,
                "/completeTask",
                encodePathVariable(instanceId),
                encodePathVariable(taskName));
    }

    String buildStartProcessUrl(HttpConfig httpConfig, ProcessInstance processInstance) {

        return String.format("%s%s%s%s",
                httpConfig.getAddress(),
                EXTENDED_PREFIX,
                "/startProcess",
                encodePathVariable(processInstance.getDefinitionId()));
    }

    private String encodePathVariable(String pathVariable) {
        try {
            return "/".concat(URLEncoder.encode(pathVariable, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    String buildDeployDefinitionUrl(HttpConfig httpConfig) {
        return String.format("%s%s", httpConfig.getAddress(), "/repository/deployments");
    }
}
