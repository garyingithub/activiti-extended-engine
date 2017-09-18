package cn.edu.sysu.workflow.cloud.load.http;


import cn.edu.sysu.workflow.cloud.load.http.async.OkHttpCallback;
import com.alibaba.fastjson.JSON;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static okhttp3.MultipartBody.Builder;

public class HttpHelper implements IHttpHelper {

    public String postFile(String url, File file, Map<String, String> headers) {
//        HttpPost httpPost = new HttpPost();
//        httpPost.setURI(URI.create(httpConfig.getAddress().concat(url)));
//        MultipartEntityBuilder mEntityBuilder = MultipartEntityBuilder.create();
//        mEntityBuilder.addBinaryBody(file.getName(), file);
//        httpPost.setEntity(mEntityBuilder.build());
//        return sendRequest(httpPost, headers);

        RequestBody requestBody = new Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse(Constants.CONTENT_TYPE_FORM_URL), file))
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void asyncPostObject(String url, Object object, Map<String, String> headers, OkHttpCallback callback) {
        Request.Builder requestBuilder = new Request.Builder();
        headers.forEach(requestBuilder::addHeader);
        requestBuilder.url(url);
        requestBuilder.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(object)));
        Request request = requestBuilder.build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                throw new RuntimeException(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.call(call, response);
                response.close();
            }
        });
    }


    private OkHttpClient okHttpClient = new OkHttpClient();
    public String postParams(String url, Map<String, ?> params, Map<String, String> headers) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        params.forEach((key, value) -> formBuilder.add(key, JSON.toJSONString(value)));
        RequestBody formBody = formBuilder.build();
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        headers.forEach(requestBuilder::addHeader);
        requestBuilder.url(url);
        requestBuilder.post(formBody);


        try {
            Response response = okHttpClient.newCall(requestBuilder.build()).execute();
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String postObject(String url, Object object, Map<String, String> headers) {
        RequestBody body = RequestBody.create(MediaType.parse(Constants.CONTENT_TYPE_JSON_URL), JSON.toJSONString(object));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String url, Map<String, String> headers) {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();


        try {
            Response response = okHttpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }



}
