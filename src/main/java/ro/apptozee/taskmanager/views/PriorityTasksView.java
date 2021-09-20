package ro.apptozee.taskmanager.views;

import ro.apptozee.taskmanager.TasksView;
import ro.apptozee.taskmanager.vo.PID;
import ro.apptozee.taskmanager.vo.Priority;
import ro.apptozee.taskmanager.vo.SortOrder;
import ro.apptozee.taskmanager.vo.Task;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class PriorityTasksView implements TasksView {
    private final TreeMap<Priority, Map<PID, Task>> tasks;
    private final SortOrder sortOrder;

    public PriorityTasksView(TreeMap<Priority, Map<PID, Task>> tasks, SortOrder sortOrder) {
        this.tasks = tasks;
        this.sortOrder = sortOrder;
    }

    @Override
    public void list(Consumer<Task> consumer) {
        if (sortOrder == SortOrder.ASCENDING) {
             tasks.entrySet().stream().flatMap(kv->kv.getValue().values().stream()).forEach(consumer);
        } else {
             tasks.descendingMap().entrySet().stream().flatMap(kv->kv.getValue().values().stream()).forEach(consumer);
        }
    }
}
