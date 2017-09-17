package cn.edu.sysu.workflow.cloud.load.http;

import java.io.File;
import java.util.Map;

public interface IHttpHelper {
    String postParams(String url, Map<String, ?> params, Map<String, String> headers);

    String postObject(String url, Object object, Map<String, String> headers);

    String get(String url, Map<String, String> headers);

    String postFile(String url, File file, Map<String, String> headers);

}
