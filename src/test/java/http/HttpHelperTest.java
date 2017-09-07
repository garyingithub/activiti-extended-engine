package http;

import org.junit.Before;

import static org.junit.Assert.*;

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

    @org.junit.Test
    public void get() throws Exception {
//        HttpHelper httpHelper = new HttpHelper(config);
//        assert(!httpHelper.get("").isEmpty());
    }

}