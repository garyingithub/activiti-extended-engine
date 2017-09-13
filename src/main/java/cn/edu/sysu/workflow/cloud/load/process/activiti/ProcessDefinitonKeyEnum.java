package cn.edu.sysu.workflow.cloud.load.process.activiti;

public enum ProcessDefinitonKeyEnum {
    TEST_USER_TASK("testUserTasks:2:14");

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    ProcessDefinitonKeyEnum(String key) {
        this.key = key;
    }
}
