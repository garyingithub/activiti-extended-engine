package cn.edu.sysu.workflow.cloud.load.engine.activiti;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedActivitiTest {
    @Test
    public void dfs() throws Exception {
        DistributedActiviti distributedActiviti = new DistributedActiviti(new ArrayList<>());

        Integer[] test0 = {5, 4, 3};
        Integer[] test1 = {6, 4, 4};
        Integer[][] serverArray = new Integer[2][3];
        serverArray[0] = test0;
        serverArray[1] = test1;

        List<Integer> instance = new ArrayList<>();
        instance.add(3);
        instance.add(2);

        List<List<Integer>> instanceList = new ArrayList<>();
        instanceList.add(instance);
        instanceList.add(instance);
        instanceList.add(instance);
        instanceList.add(instance);
        instanceList.add(instance);
        instanceList.add(instance);

        AtomicInteger maxWorkload = new AtomicInteger(0);
        List<Integer> result = new ArrayList<>();
//        distributedActiviti.bufferedFirstFit(maxWorkload, 0, serverArray, instanceList, new ArrayList<>(instanceList.size()), 0, result);

//        distributedActiviti.dfs(maxWorkload, 0, serverArray, instanceList, new ArrayList<>(instanceList.size()), 0, result);
//        assertTrue(maxWorkload.get() >= 5);
    }

}