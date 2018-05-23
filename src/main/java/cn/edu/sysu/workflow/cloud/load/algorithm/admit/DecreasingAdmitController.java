package cn.edu.sysu.workflow.cloud.load.algorithm.admit;

import cn.edu.sysu.workflow.cloud.load.Constant;
import cn.edu.sysu.workflow.cloud.load.balance.AdmitEnvironment;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.edu.sysu.workflow.cloud.load.Constant.getTenantId;

public class DecreasingAdmitController implements AdmitController {

    private long calculate(ProcessInstance instance) {
        long result = 0;
        for(int i = 0; i < instance.getFrequencyList().length; i++) {
            result += instance.getFrequencyList()[i] * instance.getFrequencyList()[i];
        }
        return result;
    }


    @Override
    public boolean[] admitControl(AdmitEnvironment admitEnvironment,
                                  List<ProcessInstance> processInstances) {

        List<List<ProcessInstance>> tenantInstances = new ArrayList<>();

        Map<ProcessInstance, Integer> posMap = new HashMap<>();

        for(int i = 0; i < Constant.TENANT_NUMBER; i++) {
            tenantInstances.add(new ArrayList<>());
        }

        for(int i = 0; i < processInstances.size(); i++) {
//            int tenantId = processInstances.get(i).getId() % Constant.TENANT_NUMBER;
            int tenantId = getTenantId(processInstances.get(i));

            tenantInstances.get(tenantId).add(processInstances.get(i));
            posMap.put(processInstances.get(i), i);
        }

        boolean[] result = new boolean[processInstances.size()];
        Server temp = new Server(50);
        for(int i = Constant.TENANT_NUMBER - 1; i >= 0; i--) {

            float weight = calculateWeight(i);
            boolean[] tempResult = controlTenant(weight, temp,
                    tenantInstances.get(i));
            for(int j = 0; j < tempResult.length; j++) {
                result[posMap.get(tenantInstances.get(i).get(j))] = tempResult[j];
                if(!admitEnvironment.checkOverload(processInstances.get(i))) {
                    admitEnvironment.assignProcessInstance(tenantInstances.get(i).get(j));
                    admitEnvironment.admitProcessInstance(tenantInstances.get(i).get(j));
                }
            }
        }
        return result;
//        processInstances.sort(Comparator.comparingLong(this::calculate));
//        return new GreedyAdmitController().admitControl(admitEnvironment, processInstances);
    }

    private boolean[] controlTenant(float weight, Server server, List<ProcessInstance> instances) {
        int[] volumes = server.getCapacityCopy();
        int[] share = new int[volumes.length];
        int[] gain = new int[volumes.length];

        for(int i = 0; i < share.length; i++) {
            share[i] = Float.valueOf(weight * volumes[i]).intValue();
        }

        boolean[] result = new boolean[instances.size()];
        for(int i = 0; i < instances.size(); i++) {
            boolean ok = true;
            for(int j = 0; j < share.length; j++) {
                if(j < instances.get(i).getFrequencyList().length) {
                    if(gain[j] + instances.get(i).getFrequencyList()[j] < share[j]) {
                        gain[j] +=instances.get(i).getFrequencyList()[j];
                    } else {
                        ok = false;
                        break;
                    }
                }
            }
            result[i] = ok;
            for(int j = 0; j < share.length; j++) {
                if(j < instances.get(i).getFrequencyList().length) {
                    server.deployWorkload(instances.get(i).getFrequencyList());
                }
            }
        }
        return result;
    }

    private float calculateWeight(int i) {
        float sum = 0;
        for(int j = 0; j <= i; j++) {
            sum += Constant.TENANT_WEIGHTS[j];
        }
        return Constant.TENANT_WEIGHTS[i] / sum;
    }
    class Tenant {
        List<ProcessInstance> processInstances;
        double weight;

        int dominantTimeSlot;

        public Tenant(List<ProcessInstance> processInstances, Server server) {
            int[] remaining = server.getCapacityCopy();

            int[] wanted = new int[remaining.length];

            processInstances.forEach(instance -> {
                for(int i = 0; i < remaining.length; i++) {
                    if(i < instance.getFrequencyList().length) {
                        wanted[i] += instance.getFrequencyList()[i];
                    }
                }
            });

            int maxPos = 0;
            float maxV = 0;
            for(int i = 0; i < remaining.length; i++) {
                float share = Float.valueOf(wanted[i]) / remaining[i];
                if (share > maxV) {
                    maxPos = i;
                    maxV = share;
                }
            }
            dominantTimeSlot = maxPos;
        }
    }
}
