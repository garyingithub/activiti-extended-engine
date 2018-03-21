package cn.edu.sysu.workflow.cloud.load.balance;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.TimeFollower;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.Server;

import java.util.Arrays;

public class AdmitEnvironment implements TimeFollower {
    private Server server;

    private Tenant getTenant(ProcessInstance instance) {
        return tenants[Constant.getTenantId(instance)];
    }

    private Tenant[] tenants;


    public AdmitEnvironment(int capacity) {
        this.server = new Server(capacity);
        tenants = new Tenant[Constant.TENANT_NUMBER];
        for(int i = 0; i < tenants.length; i++) {
            tenants[i] = new Tenant(Constant.TENANT_WEIGHTS[i]);
        }
    }

    public void admitProcessInstance(ProcessInstance instance) {
        getTenant(instance).admitProcessInstance(instance);
        getServer().deployWorkload(instance.getFrequencyList());

    }

    public void assignProcessInstance(ProcessInstance instance) {
        getTenant(instance).assignProcessInstance(instance);
    }

    public Server getServer() {
        return server;
    }

    @Override
    public void pastPeriod() {
        Arrays.stream(tenants).forEach(Tenant::pastPeriod);
        server.pastPeriod();
    }

    public boolean checkDominantOverload(ProcessInstance processInstance) {
        return getTenant(processInstance).checkDominantOverload(processInstance);
    }

    public boolean checkOverload(ProcessInstance processInstance) {
        for(int i = 0; i < server.getRemainingCapacity().length; i++) {
            if(i < processInstance.getFrequencyList().length) {
                if(server.getRemainingCapacity()[i] < processInstance.getFrequencyList()[i]) {
                    return true;
                }
            }
        }
        return false;
    }




    class Tenant implements TimeFollower{
        float weight;
        int[] apply;
        int[] gain;

        Tenant(float weight) {
            this.weight = weight;
            this.apply = new int[Constant.WATCHED_PERIOD_NUMBER];
            this.gain = new int[Constant.WATCHED_PERIOD_NUMBER];
        }
        void assignProcessInstance(ProcessInstance instance) {
            for(int i = 0; i < instance.getFrequencyList().length; i++) {
                if(apply.length > i) {
                    apply[i] += instance.getFrequencyList()[i];
                }
            }
        }

        void admitProcessInstance(ProcessInstance instance) {
            for(int i = 0; i < instance.getFrequencyList().length; i++) {
                if(gain.length > i) {
                    gain[i] += instance.getFrequencyList()[i];
                }
            }
        }

        boolean checkDominantOverload(ProcessInstance processInstance) {
//            int maxV = Arrays.stream(apply).max().getAsInt();
//            for(int i = 0 ; i < apply.length; i++) {
//                if(this.apply[i] == maxV) {
//                    return processInstance.getFrequencyList().length > i &&
//                            (gain[i] + processInstance.getFrequencyList()[i] >
//                                    Math.floor(weight * Constant.ENGINE_CAPACITY));
//
//                }
//            }

            for(int i = 0; i < gain.length; i++) {
                if(i < processInstance.getFrequencyList().length) {
                    if(gain[i] + processInstance.getFrequencyList()[i] >
                            Math.floor(weight * Constant.ENGINE_CAPACITY)) {
                        return true;
                    }
                }
            }
            return false;
        }


        @Override
        public void pastPeriod() {
            Constant.pastPeriod(apply, 0);
            Constant.pastPeriod(gain, 0);
        }
    }
}
