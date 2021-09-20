package ro.apptozee.taskmanager;

import ro.apptozee.taskmanager.vo.PID;
import ro.apptozee.taskmanager.vo.Priority;

public record Task(PID pid, Priority priority, TaskManager taskManager) {

    public void kill(){
        // code that performs the actual kill command to be inserted here

        // TODO avoid loop
        this.taskManager.kill(this);
    }

}
