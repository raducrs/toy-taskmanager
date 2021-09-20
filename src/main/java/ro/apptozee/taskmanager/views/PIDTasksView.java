package ro.apptozee.taskmanager.views;

import ro.apptozee.taskmanager.TasksView;
import ro.apptozee.taskmanager.vo.SortOrder;
import ro.apptozee.taskmanager.vo.Task;

import java.util.Spliterators;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

public class PIDTasksView implements TasksView {
    private final SortOrder sortOrder;
    private final TreeSet<Task> tasks;

    public PIDTasksView(TreeSet<Task> tasks,SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        this.tasks = tasks;
    }


    @Override
    public void list(Consumer<Task> consumer) {
        if (sortOrder == SortOrder.ASCENDING) {
            StreamSupport.stream(Spliterators.spliteratorUnknownSize(tasks.iterator(), 0), false).forEach(consumer);
        } else {
            StreamSupport.stream(Spliterators.spliteratorUnknownSize(tasks.descendingIterator(), 0), false).forEach(consumer);;
        }
    }
}
