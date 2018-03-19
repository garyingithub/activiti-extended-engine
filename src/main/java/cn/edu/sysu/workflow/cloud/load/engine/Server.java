package cn.edu.sysu.workflow.cloud.load.engine;

import cn.edu.sysu.workflow.cloud.load.DistributedLogSimulator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.edu.sysu.workflow.cloud.load.Constant.PERIOD;

public class Server {

    private List<Integer> capacityList = new CopyOnWriteArrayList<>();

    private long id;
    private int size;
    private int capacity;
    private static List<Server> servers = new CopyOnWriteArrayList<>();
    private static Executor refreshExecutor = Executors.newSingleThreadExecutor();

    static {
        refreshExecutor.execute(() -> {
            while (true) {
                long start = System.currentTimeMillis();
                servers.parallelStream().forEach(Server::refresh);
//                System.out.println(Activiti.processedCount);
                try {
                    TimeUnit.MILLISECONDS.sleep(PERIOD - (System.currentTimeMillis() - start));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    private AtomicInteger processCount = new AtomicInteger();

    public void addLoad(List<Integer> load) {
        processCount.getAndAdd(1);
//        if (load.size() > size) {
//            throw new RuntimeException("Can be bigger than server's size");
//        }
        int i = 0;
        for (Integer aLoad : load) {
            if (i >= capacityList.size()) {break;}
            capacityList.set(i, capacityList.get(i) - aLoad);
            i++;
        }
    }

    public boolean canAdd(List<Integer> load) {
        int i = 0;
        for (Integer aLoad : load) {
            if (capacityList.get(i) < aLoad) {
                return false;
            }
            i++;
        }
        return true;
    }

    private void refresh() {
        this.capacityList.add(capacityList.size() - 1, capacity);
        this.capacityList.remove(0);
    }

    public Server(int id, int size, int capacity) {
        this.size = size;
        this.capacity = capacity;
        this.id = id;

        for (int i = 0; i < size; i++) {
            capacityList.add(capacity);
        }
        servers.add(this);
    }

    public Server(int id) {
        this(id, 20, DistributedLogSimulator.capacity);
    }

    public long getId() {
        return id;
    }

//    public long getSpace() {
//        long a = 0;
//        for (Integer c : capacityList) {
//            a += c;
//        }
//        return a;
//    }

    public Integer[] getCapacityArray() {
        Integer[] capacityArray = new Integer[capacityList.size()];
        for (int i = 0; i < capacityList.size(); i++) {
            capacityArray[i] = capacityList.get(i);
        }
        return capacityArray;
    }

//    private int getDistance(Integer[] a, Integer[] b, int size) {
//        Integer[] aCopy = Arrays.copyOf(a, size);
//        Integer[] bCopy = Arrays.copyOf(b, size);
//
//        int aMax = Arrays.stream(aCopy).min(Comparator.naturalOrder()).get();
//        int bMax = Arrays.stream(bCopy).min(Comparator.naturalOrder()).get();
//        return Math.max(aMax, bMax);
//    }


}
