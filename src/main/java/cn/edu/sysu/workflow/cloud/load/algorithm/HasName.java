package cn.edu.sysu.workflow.cloud.load.algorithm;

public interface HasName {

    default String getName() {
        return "局部离线流程实例准入控制算法";
    }

}
