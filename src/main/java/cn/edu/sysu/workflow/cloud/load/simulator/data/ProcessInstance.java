package cn.edu.sysu.workflow.cloud.load.simulator.data;

import org.w3c.dom.Element;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ProcessInstance {

    public static final long PERIOD = 60000;
    private List<Task> tasks;
    private String definitionId;

    private List<Integer> frequencyList = new LinkedList<>();
    public static class Task {
        private String taskName;

        private long start;
        private long end;
        private boolean available = true;

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public long getDuration() {
            return end - start;
        }


        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public long getStart() {
            return start;
        }

        public void setStart(long start) {
            this.start = start;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(long end) {
            this.end = end;
        }
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


        int size = new Long((Collections.max(tasks, Comparator.comparingLong(o -> o.end)).getEnd() - tasks.get(0).start) / PERIOD + 1).intValue();

        long start = tasks.get(0).start;
        int[] frequencyArray = new int[size];
        tasks.forEach(task -> {
            frequencyArray[new Long((task.start - start) / PERIOD).intValue()]++;
            frequencyArray[new Long((task.end - start) / PERIOD).intValue()]++;
        });

        this.tasks = tasks;
        System.out.println(frequencyArray.length);
    }
}
