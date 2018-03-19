package cn.edu.sysu.workflow.cloud.load.balance;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.TimeFollower;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.Server;

import java.util.Arrays;

public class AdmitEnvironment implements TimeFollower {
    private Server server;

    private Tenant getTenant(ProcessInstance instance) {
        return tenants[instance.getId() % tenants.length];
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
    }

    public void assignProcessInstance(ProcessInstance instance) {
        getTenant(instance).assignProcessInstance(instance);
    }

    public Server getServer() {
        return server;
    }

    @Override
    public void pastPeriod() {
        Arrays.stream(tenants).forEach(tenant -> {
            tenant.pastPeriod();
            server.pastPeriod();
        });
        server.pastPeriod();
    }

    public boolean checkDominantOverload(ProcessInstance processInstance) {
        return getTenant(processInstance).checkDominantOverload(processInstance);
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
            int maxV = Arrays.stream(apply).max().getAsInt();
            for(int i = 0 ; i < apply.length; i++) {
                if(this.apply[i] == maxV) {
                    return processInstance.getFrequencyList().length > i &&
                            (gain[i] + processInstance.getFrequencyList()[i] >
                                    Math.floor(weight * Constant.ENGINE_CAPACITY));

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
