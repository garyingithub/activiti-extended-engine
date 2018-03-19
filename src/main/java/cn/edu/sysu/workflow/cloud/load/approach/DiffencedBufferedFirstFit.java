package cn.edu.sysu.workflow.cloud.load.approach;

import cn.edu.sysu.workflow.cloud.load.DistributedLogSimulator;
import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiffencedBufferedFirstFit implements Allocator {

    int[] weights = new int[] {30, 20, 10};

    private final static int SLOT_CAPACITY = 60;

    private int[] lastCapacity = new int[DistributedLogSimulator.ports.length];

    @Override
    public void allocate(Integer[][] serverCapacityArray, List<ProcessInstance> instanceBuffer, List<Integer> result) {

        Integer[][] slot = new Integer[1][serverCapacityArray[0].length];

        Array.set(slot[0], 0, SLOT_CAPACITY);

        OptionalInt engine = Arrays.stream(new int[] {0, 1, 2}).filter(value -> lastCapacity[value] > SLOT_CAPACITY).findFirst();

        if(!engine.isPresent()) {
            for(int i = 0; i < instanceBuffer.size(); i++) {
                result.add(-1);
            }
        }
        new BufferedFirstFit().allocate(slot, instanceBuffer, result);

        result.stream().map(integer -> integer == -1 ? -1 : engine.getAsInt());

        lastCapacity[engine.getAsInt()] -= SLOT_CAPACITY;

        releaseSlot(engine.getAsInt(), 20);

    }

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private void releaseSlot(int i, int maxExecuteTime) {
        scheduledExecutorService.schedule(() -> {
            lastCapacity[i] += SLOT_CAPACITY;
        }, maxExecuteTime, TimeUnit.MINUTES);
    }
}
