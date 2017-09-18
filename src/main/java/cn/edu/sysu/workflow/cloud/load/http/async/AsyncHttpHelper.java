package cn.edu.sysu.workflow.cloud.load.http.async;

import cn.edu.sysu.workflow.cloud.load.http.HttpConfig;
import cn.edu.sysu.workflow.cloud.load.http.IHttpHelper;
import com.alibaba.fastjson.JSON;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Callable;

public class AsyncHttpHelper implements IHttpHelper {

    private OkHttpClient client = new OkHttpClient();
    private HttpConfig config;

    public AsyncHttpHelper(HttpConfig httpConfig) {
        this.config = httpConfig;
    }

    @Override
    public String postParams(String url, Map<String, ?> params, Map<String, String> headers) {
        return null;
    }

    @Override
    public String postObject(String url, Object object, Map<String, String> headers, OkHttpCallback okHttpCallback) {

        Request.Builder requestBuilder = new Request.Builder();
        headers.forEach(requestBuilder::addHeader);
        requestBuilder.url(config.getAddress().concat(url));
        requestBuilder.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(object)));
        Request request = requestBuilder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                throw new RuntimeException(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                okHttpCallback.call(call, response);
                response.close();
            }
        });
        return null;
    }

    @Override
    public String get(String url, Map<String, String> headers) {
        return null;
    }

    @Override
    public String postFile(String url, File file, Map<String, String> headers) {
        return null;
    }
}
