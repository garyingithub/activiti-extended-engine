package cn.edu.sysu.workflow.cloud.load.simulation;


import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import cn.edu.sysu.workflow.cloud.load.engine.BasicEngine;

import java.util.HashMap;
import java.util.Map;

public class SimulateEnvironment {

    private SimulateServer[] engines;
    private Map<Long, SimulateServer> serverMap= new HashMap<>();
    private int historyLength;

    private int[] numberHistory;
    public SimulateEnvironment( int historyLength) {
        this.historyLength = historyLength;
    }

    public int getEngineNumber() {
        return serverMap.size();
    }
    public void launchProcessInstance(ProcessInstance instance, BasicEngine server, int current) {
        serverMap.putIfAbsent(server.getId(), new SimulateServer(historyLength, server));
        serverMap.get(server.getId()).deployRecord(current, instance);
    }

    public int[][] getHistory() {
        int[][] result = new int[serverMap.size()][];

        int count = 0;
        for(long key : serverMap.keySet()) {
            result[count] = serverMap.get(key).history;
            count++;
        }

        return result;
    }

    class SimulateServer {

        private long id;
        SimulateServer(int historyLength, BasicEngine basicEngine) {
            this.id = basicEngine.getId();
            this.history = new int[historyLength];
        }
        int[] history;

        void deployRecord(int current, ProcessInstance processInstance) {
//            processInstance.recordTimeSlot = current;
            for(int i = 0; i < processInstance.getFrequencyList().length; i++) {
                history[current + i] += processInstance.getFrequencyList()[i];
            }
        }
    }
}
