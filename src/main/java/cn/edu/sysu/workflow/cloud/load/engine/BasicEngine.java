package cn.edu.sysu.workflow.cloud.load.engine;

import cn.edu.sysu.workflow.cloud.load.TimeFollower;
import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;

public abstract class BasicEngine implements WorkflowEngine, TimeFollower {

    protected Server server;

    public long getId() {
        return server.getId();
    }

    public boolean checkOverload(ProcessInstance processInstance) {
        return server.checkOverload(processInstance.getFrequencyList());
    }

    public int[] getRemainingCapacity() {
        return Arrays.copyOf(server.getRemainingCapacity(), server.getRemainingCapacity().length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BasicEngine engine = (BasicEngine) o;

        return new EqualsBuilder()
                .append(server, engine.server)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(server)
                .toHashCode();
    }
}
