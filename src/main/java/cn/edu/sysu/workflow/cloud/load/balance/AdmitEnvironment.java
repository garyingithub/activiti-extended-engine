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
//            tenants[i] = new Tenant(Constant.TENANT_WEIGHTS[i]);
            tenants[i] = new Tenant(Constant.TENANT_WEIGHTS[i % Constant.TENANT_WEIGHTS.length] );

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

    public boolean checkWeightedOverload(ProcessInstance processInstance) {
        return getTenant(processInstance).checkWeightedOverload(processInstance);
    }

    public boolean checkWeightedOverload2(ProcessInstance processInstance) {
        return getTenant(processInstance).checkWeightedOverload2(processInstance);
    }

    public boolean checkOverload(ProcessInstance processInstance) {
        for(int i = 0; i < server.getCapacityCopy().length; i++) {
            if(i < processInstance.getFrequencyList().length) {
                if(server.getCapacityCopy()[i] < processInstance.getFrequencyList()[i]) {
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

            for(int i = 0; i < gain.length; i++) {
                if(i < processInstance.getFrequencyList().length) {
                    int nextGain = gain[i] + processInstance.getFrequencyList()[i];

                    if(nextGain >
                            Math.floor(weight * Constant.ENGINE_CAPACITY * Constant.TENANT_NUMBER)) {
                        return true;
                    }
                }
            }
            return false;
        }

//        private double[] weights = new double[] {2, 1,5, 1.33, 1.25, 1.2, 1.17, 1.14};
        private double[] weights = new double[] {2, 1.5, 1.25};

        boolean checkWeightedOverload(ProcessInstance processInstance) {
            for(int i = 0; i < gain.length; i++) {
                if(i < processInstance.getFrequencyList().length) {
                    int nextGain = gain[i] + processInstance.getFrequencyList()[i];

                    double slotWeight = i < weights.length ? weights[i] : 1;
                    if(nextGain > slotWeight * Math.floor(weight * Constant.ENGINE_CAPACITY * Constant.TENANT_NUMBER)) {
                        return true;
                    }
//                    int threshold = Math.floorDiv(server.getCapacity(), 10);
//
//                    if(server.getCapacityCopy()[i] < threshold) {
//                        if(nextGain > Math.floor(weight * Constant.ENGINE_CAPACITY)) {
//                            return true;
//                        }
//                    } else {
//                        double slotWeight = i < weights.length ? weights[i] : 1;
//                        if(nextGain > slotWeight * Math.floor(weight * Constant.ENGINE_CAPACITY * Constant.TENANT_NUMBER)) {
//                            return true;
//                        }
//                    }

                }
            }
            return false;
        }

//        private double[] weights = new double[] {2, 1.5, 1.25};

        boolean checkWeightedOverload2(ProcessInstance processInstance) {
            for(int i = 0; i < gain.length; i++) {
                if(i < processInstance.getFrequencyList().length) {
                    int nextGain = gain[i] + processInstance.getFrequencyList()[i];
//
//                    double slotWeight = i < weights.length ? weights[i] : 1;
//                    if(nextGain > slotWeight * Math.floor(weight * Constant.ENGINE_CAPACITY * Constant.TENANT_NUMBER)) {
//                        return true;
//                    }
                    int threshold = Math.floorDiv(server.getCapacity(), 10);

                    if(server.getCapacityCopy()[i] < threshold) {
                        if(nextGain > Math.floor(weight * Constant.ENGINE_CAPACITY)) {
                            return true;
                        }
                    } else {
                        double slotWeight = i < weights.length ? weights[i] : 1;
                        if(nextGain > slotWeight * Math.floor(weight * Constant.ENGINE_CAPACITY * Constant.TENANT_NUMBER)) {
                            return true;
                        }
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
