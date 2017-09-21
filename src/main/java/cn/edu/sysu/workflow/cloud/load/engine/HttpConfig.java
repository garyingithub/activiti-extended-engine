package cn.edu.sysu.workflow.cloud.load.engine;

import java.io.Serializable;

public class HttpConfig implements Serializable{

    private static final long serialVersionUID = -5497139673523768421L;
    private  String host;
    private String port;
    private int maxTotal = 200;
    private int socketTimeout;
    private int connectionTimeout;
    private int connectionRequestTimeout;

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public String getHost() {
        return host;
    }

    public String getAddress() {

        return"http://" +  host + ":" + port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
