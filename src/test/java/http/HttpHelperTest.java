package http;

import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import org.junit.Before;

import java.util.concurrent.CountDownLatch;

public class HttpHelperTest {

    HttpConfig config;
    @Before
    public void init () {
        HttpConfig config = new HttpConfig();
        config.setHost("qq.com");
        config.setPort("80");
        this.config = config;
    }
    @org.junit.Test
    public void postParams() throws Exception {

    }

    @org.junit.Test
    public void postObject() throws Exception {
    }

    class TestRunnable implements Runnable {

        private CountDownLatch barrier;

        TestRunnable(CountDownLatch barrier) {
            this.barrier = barrier;
        }


        @Override
        public void run() {
            long i = 0;
            while (i < Long.MAX_VALUE) {
                i++;
            }

            barrier.countDown();

        }
    }

    @org.junit.Test
    public void get() throws Exception {
        int number = 8;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        for (int i = 0; i < number; i++) {
            new Thread(new TestRunnable(countDownLatch)).start();
        }
        countDownLatch.await();
//        AbstractList
       
    }

}