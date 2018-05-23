package cn.edu.sysu.workflow.cloud.load.graph;

public enum GraphType {
    UTILIZATION("时间(分钟)", "CPU利用率"),
    SLA("时间(分钟)", "SLA违反率"),
    OVERUSE("时间(分钟)", "超额比"),
    USED_ENGINE("流程实例数", "使用的引擎数");

    private String xLabel;
    private String yLabel;

    GraphType(String xLabel, String yLabel) {
        this.xLabel = xLabel;
        this.yLabel = yLabel;
    }

    public String getXLabel() {
        return xLabel;
    }

    public String getYLabel() {
        return yLabel;
    }
}
