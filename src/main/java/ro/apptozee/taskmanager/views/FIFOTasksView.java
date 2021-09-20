package ro.apptozee.taskmanager.views;

import ro.apptozee.taskmanager.TasksView;
import ro.apptozee.taskmanager.vo.PID;
import ro.apptozee.taskmanager.vo.SortOrder;
import ro.apptozee.taskmanager.vo.Task;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FIFOTasksView implements TasksView {
    private final LinkedHashMap<PID, Task> tasks;
    private final SortOrder sortOrder;

    public FIFOTasksView(LinkedHashMap<PID, Task> tasks, SortOrder sortOrder) {
        this.tasks = tasks;
        this.sortOrder = sortOrder;
    }

    @Override
    public void list(Consumer<Task> consumer) {
        if (sortOrder == SortOrder.ASCENDING) {
            tasks.entrySet().stream().map(Map.Entry::getValue).forEach(consumer);
        } else {
            // can not do any better than this without accessing internal state unfortunately (since we do not have descending iterators)
            var reversed = tasks.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
            Collections.reverse(reversed);
            reversed.stream().forEach(consumer);
        }
    }
}
