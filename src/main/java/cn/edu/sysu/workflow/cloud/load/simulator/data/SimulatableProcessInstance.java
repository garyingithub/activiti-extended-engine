package cn.edu.sysu.workflow.cloud.load.simulator.data;

public class SimulatableProcessInstance extends ProcessInstance {

    private TraceNode trace;

    public TraceNode getTrace() {
        return trace;
    }

    public void setTrace(TraceNode trace) {
        this.trace = trace;
    }

    public SimulatableProcessInstance() {

    }


}
