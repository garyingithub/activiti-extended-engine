package cn.edu.sysu.workflow.cloud.load.data;

import cn.edu.sysu.workflow.cloud.load.Constant;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static cn.edu.sysu.workflow.cloud.load.Constant.PERIOD;

public class ProcessInstance {

    private int id;

    private int timeSlot;

    public ProcessInstance() {
        this.id = Constant.INSTANCE_ID_GENERATOR.getAndAdd(1);
    }
    private List<Task> tasks;
    private String definitionId;

    public int getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(int timeSlot) {
        this.timeSlot = timeSlot;
    }

    private int[] frequencyList;

    public int[] getFrequencyList() {
        return frequencyList;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {

        long end = Collections.max(tasks, Comparator.comparingLong(o -> o.end)).end;
        long start = tasks.get(0).start;

        int size = Long.valueOf((end - start) / PERIOD + 1).intValue();

        int[] frequencyArray = new int[size];
        tasks.forEach(task -> {
            frequencyArray[Long.valueOf((task.start - start) / PERIOD).intValue()]++;
            frequencyArray[Long.valueOf((task.end - start) / PERIOD).intValue()]++;
        });
        this.frequencyList = frequencyArray;
        this.tasks = tasks;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProcessInstance that = (ProcessInstance) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(tasks, that.tasks)
                .append(definitionId, that.definitionId)
                .append(frequencyList, that.frequencyList)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(tasks)
                .append(definitionId)
                .append(frequencyList)
                .toHashCode();
    }
}
