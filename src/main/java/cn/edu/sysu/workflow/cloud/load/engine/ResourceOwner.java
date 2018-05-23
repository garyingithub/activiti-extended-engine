package cn.edu.sysu.workflow.cloud.load.engine;

import cn.edu.sysu.workflow.cloud.load.TimeFollower;

public interface ResourceOwner extends TimeFollower {

    boolean deployWorkload(final int[] workload);
    boolean checkOverload(final int[] workload);
}
