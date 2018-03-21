package cn.edu.sysu.workflow.cloud.load.simulation;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.algorithm.admit.AdmitController;
import cn.edu.sysu.workflow.cloud.load.algorithm.scheduling.Scheduler;
import cn.edu.sysu.workflow.cloud.load.algorithm.scheduling.TenantBasedScheduler;
import cn.edu.sysu.workflow.cloud.load.balance.LoadBalancer;
import cn.edu.sysu.workflow.cloud.load.engine.BasicEngine;
import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;

public enum LoadBalancerGenerator {

    INSTANCE;

    LoadBalancer testUtilization(AdmitController controller) {
        SimulateActiviti activiti = new SimulateActiviti(Constant.ENGINE_CAPACITY, new HttpConfig());
        LoadBalancer loadBalancer = new LoadBalancer(new BasicEngine[] {activiti}, Constant.ENGINE_CAPACITY);
        loadBalancer.setAdmitController(controller);
        loadBalancer.setScheduler(new TenantBasedScheduler());
        return loadBalancer;
    }

    LoadBalancer testFairness(AdmitController controller) {
        BasicEngine[] engines = new BasicEngine[Constant.TENANT_NUMBER];

        for(int i = 0; i < engines.length; i++) {
            engines[i] = new SimulateActiviti(Constant.ENGINE_CAPACITY, new HttpConfig());
        }

        LoadBalancer loadBalancer = new LoadBalancer(engines,
                Constant.ENGINE_CAPACITY * Constant.TENANT_NUMBER);
        loadBalancer.setAdmitController(controller);
        loadBalancer.setScheduler(new TenantBasedScheduler());
        return loadBalancer;
    }

    LoadBalancer testScheduling(Scheduler scheduler) {

        return testUsedEnginesWithCapacity(scheduler, Constant.ENGINE_CAPACITY);
    }

    LoadBalancer testUsedEnginesWithCapacity(Scheduler scheduler, int capacity) {
        BasicEngine[] engines = new BasicEngine[1];

        for(int i = 0; i < engines.length; i++) {
            engines[i] = new SimulateActiviti(capacity, new HttpConfig());
        }
        LoadBalancer loadBalancer = new LoadBalancer(engines, Constant.ENGINE_CAPACITY);
        loadBalancer.setAdmitController(new AdmitController() {});
        loadBalancer.setScheduler(scheduler);
        return loadBalancer;
    }
}
