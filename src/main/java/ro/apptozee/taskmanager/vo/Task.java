package ro.apptozee.taskmanager.vo;

import ro.apptozee.taskmanager.TaskManager;
import ro.apptozee.taskmanager.vo.PID;
import ro.apptozee.taskmanager.vo.Priority;

import java.util.Comparator;

public record Task(PID pid, Priority priority, TaskManager taskManager) {

    public static final Comparator<Task> BY_PID_COMP = Comparator.comparing(Task::pid);

    public void kill(){
        // code that performs the actual kill command to be inserted here

        // there is a potential infinite loop here
        // we avoided it by making sure we remove the task from internal tracking before issuing the kill command
        this.taskManager.kill(this);
    }

}
