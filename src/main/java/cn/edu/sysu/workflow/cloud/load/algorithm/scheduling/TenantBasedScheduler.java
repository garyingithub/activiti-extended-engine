package cn.edu.sysu.workflow.cloud.load.algorithm.scheduling;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.balance.AsynCallback;
import cn.edu.sysu.workflow.cloud.load.balance.ScheduleEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.BasicEngine;

import java.util.List;

import static cn.edu.sysu.workflow.cloud.load.Constant.getTenantId;

public class TenantBasedScheduler implements Scheduler {

    @Override
    public int schedule(ScheduleEnvironment environment, List<ProcessInstance> processInstances, AsynCallback callback) {

        for(int i = 0; i < processInstances.size(); i++) {
            ProcessInstance processInstance = processInstances.get(i);
//            int tenantId = processInstance.getId() % environment.getPool().length;
            int tenantId = getTenantId(processInstance);
            BasicEngine engine = environment.getPool().get(tenantId);
            engine.
                    generateWorkload( processInstance);
            callback.call(processInstance, engine);
        }

        return Constant.TENANT_NUMBER;
    }
}
