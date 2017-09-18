package cn.edu.sysu.workflow.cloud.load.http.async;

import okhttp3.Call;
import okhttp3.Response;

@FunctionalInterface
public interface OkHttpCallback {
    void call(Call call, Response response);
}
