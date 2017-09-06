package http;


import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Consumer;

public class HttpHelper  {
    // 连接管理器
    private PoolingHttpClientConnectionManager pool;

    // 请求配置
    private RequestConfig requestConfig;

    private HttpConfig httpConfig;

    public HttpHelper(HttpConfig httpConfig) {
        this.requestConfig = RequestConfig.custom().
                setConnectionRequestTimeout(httpConfig.getConnectionRequestTimeout()).
                setSocketTimeout(httpConfig.getSocketTimeout()).
                setConnectTimeout(httpConfig.getConnectionTimeout()).build();
                this.httpConfig = httpConfig;
                pool = new PoolingHttpClientConnectionManager();
                pool.setMaxTotal(httpConfig.getMaxTotal());
    }

    private CloseableHttpClient getHttpClient() {
        return HttpClients.custom().
                setConnectionManager(pool).
                setDefaultRequestConfig(requestConfig).
                setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).
                build();
    }


    private String sendRequest(HttpRequestBase requestBase) throws RuntimeException {
        CloseableHttpClient httpClient;
        String responseContent;
        httpClient = getHttpClient();
        requestBase.setConfig(requestConfig);
        try (CloseableHttpResponse  httpResponse = httpClient.execute(requestBase)) {
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpResponse.getStatusLine().getStatusCode() >= 300) {
                throw new RuntimeException(String.valueOf((httpResponse.getStatusLine().getStatusCode())));
            }
            responseContent = EntityUtils.toString(httpEntity, Charset.forName(Constants.CHARSET_UTF_8));
            EntityUtils.consume(httpEntity);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return responseContent;
    }

    private String postContent(String url, String content) {
        StringEntity httpEntity;
        HttpPost httpPost = new HttpPost();
        httpPost.setURI(URI.create(httpConfig.getAddress().concat(url)));
        try {
            httpEntity = new StringEntity(content);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        httpEntity.setContentType(Constants.CONTENT_TYPE_JSON_URL);
        return sendRequest(httpPost);
    }

    public String postParams(String url, Map<String, ?> params) {
        return postContent(url, stringifyParameters(params));
    }

    public String postObject(String url, Object object) {
        return postContent(url, JSON.toJSONString(object));
    }

    public String get(String url) {
        HttpGet httpGet = new HttpGet();
        httpGet.setConfig(requestConfig);
        httpGet.setURI(URI.create(httpConfig.getAddress().concat(url)));
        return sendRequest(httpGet);
    }

    private String stringifyParameters(Map<String, ?> parameterMap) {
        StringBuilder buffer = new StringBuilder();
        if (parameterMap != null) {
            parameterMap.entrySet().forEach((Consumer<Map.Entry<String, ?>>) stringEntry -> {
                buffer.append(stringEntry.getKey());
                buffer.append("=");
                buffer.append(stringEntry.getValue());
                buffer.append("&");
            });
            if (buffer.length() > 0) {
                buffer.deleteCharAt(buffer.length() - 1);
            }
        }
        return buffer.toString();
    }
}
