package cn.edu.sysu.workflow.cloud.load.http;

import cn.edu.sysu.workflow.cloud.load.Constant;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public enum HttpHelperSelector {

        SELECTOR;
        private HttpHelper[] helpers = new HttpHelper[Constant.HTTP_CONNECTIONS];
        private AtomicInteger count = new AtomicInteger(0);

        HttpHelperSelector() {
            Arrays.fill(helpers, new HttpHelper());
        }

        public HttpHelper select() {
            return helpers[count.getAndIncrement() % helpers.length];
        }
}
