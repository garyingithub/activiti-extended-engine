package cn.edu.sysu.workflow.cloud.load.simulator;

import org.w3c.dom.Element;

import java.util.List;

public class ProcessInstance {
    static class Task {
        private String taskName;
        private long duration;

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

    }

    private List<Task> tasks;
    private String definitionId;

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
        this.tasks = tasks;
    }
}
