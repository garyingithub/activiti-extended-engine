package cn.edu.sysu.workflow.cloud.load.engine;

import cn.edu.sysu.workflow.cloud.load.simulator.data.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.method.P;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class Server {

    private List<Integer> capacityList = new CopyOnWriteArrayList<>();

    private int id;
    protected int size;
    private int capacity;
    private Logger logger = LoggerFactory.getLogger(Server.class);
    //    private static long interval = 5000;
    private static List<Server> servers = new ArrayList<>();
    private static Executor refreshExecutor = Executors.newSingleThreadExecutor();

    private List<Integer> past = new ArrayList<>();

    static {
        refreshExecutor.execute(() -> {
            while (true) {
                long start = System.currentTimeMillis();
                servers.parallelStream().forEach(Server::refresh);
                try {
                    TimeUnit.MILLISECONDS.sleep(ProcessInstance.PERIOD - (System.currentTimeMillis() - start));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    private AtomicInteger processCount = new AtomicInteger();

    public void addLoad(List<Integer> load) {
        processCount.getAndAdd(1);
        if (load.size() > size) {
            throw new RuntimeException("Can be bigger than server's size");
        }
        int i = 0;
        for (Integer aLoad : load) {
            capacityList.set(i, capacityList.get(i) - aLoad);
            i++;
        }
    }

    public void refresh() {
        this.capacityList.add(capacityList.size() - 1, capacity);
        this.past.add(capacityList.get(0));
        this.capacityList.remove(0);


        synchronized (Server.class.getClassLoader()) {
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < past.size(); i++) {
                output.append(capacity - this.past.get(i));
                output.append(" ");
            }
            logger.info("server {} {}", id, output.toString());
            if (past.size() >= 5)
                past.clear();
        }


        logger.info("server {} launched {} processes", id, processCount);
        processCount.set(0);
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
        this(id, 100, 3000);
    }

    public double getStandardDiviation(Integer[] x) {
        int m = x.length;
        double sum = 0;
        for (int aX1 : x) {//求和
            sum += aX1;
        }
        double dAve = sum / m;//求平均值
        double dVar = 0;
        for (int aX : x) {//求方差
            dVar += (aX - dAve) * (aX - dAve);
        }
        return Math.sqrt(dVar / m);
    }

    Random random = new Random();

    public double getSubtractedCapacityDeviation(List<Integer> load) {
        if (load.size() > size) {
            throw new RuntimeException("Can not be bigger than server's size");
        }
//        Integer[] capacityArray = new Integer[capacityList.size()];
//        capacityList.toArray(capacityArray);
//        int i = capacityList.size() - 1;
//        int result = 0;
//        for (Integer aLoad : load) {
//            capacityArray[i] -= aLoad;
//            i--;
//        }
//        return getStandardDiviation(capacityArray);
        Integer[] a = new Integer[Server.servers.get(0).size];
        Server.servers.get(0).capacityList.toArray(a);

        Integer[] b = new Integer[Server.servers.get(1).size];
        Server.servers.get(1).capacityList.toArray(b);

        int i = 0;
        if (Server.servers.get(0).equals(this)) {
            for (Integer aLoad : load) {
                a[i] -= aLoad;
                i++;
            }
        } else {
            for (Integer aLoad : load) {
                b[i] -= aLoad;
                i++;
            }
        }
        return random.nextInt();
//        return getDistance(a, b, load.size());
    }

    public long getSpace() {
        long a = 0;
        for (Integer c : capacityList) {
            a += (capacity - c);
        }
        return a;
    }

    private int getDistance(Integer[] a, Integer[] b, int size) {
//        int result = 0;
//        for(int i = 0; i < size; i++) {
//            int temp = a[i] - b[i];
//            result +=temp * temp;
//        }
        Integer[] aCopy = Arrays.copyOf(a, size);
        Integer[] bCopy = Arrays.copyOf(b, size);
//        Arrays.copyOf(a, size);
        int aMax = Arrays.stream(aCopy).min(Comparator.naturalOrder()).get();
        int bMax = Arrays.stream(bCopy).min(Comparator.naturalOrder()).get();
        return Math.max(aMax, bMax);
//        return result;
    }


}