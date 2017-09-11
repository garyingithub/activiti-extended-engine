package cn.edu.sysu.workflow.cloud.load.process.activiti;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ProcessVariable implements Serializable{
    private static final long serialVersionUID = 6673850433358433283L;

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
