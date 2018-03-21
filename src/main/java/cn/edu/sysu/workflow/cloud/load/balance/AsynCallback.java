package cn.edu.sysu.workflow.cloud.load.balance;

import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.BasicEngine;

public interface AsynCallback {
    void call(ProcessInstance instance, BasicEngine server);
}
