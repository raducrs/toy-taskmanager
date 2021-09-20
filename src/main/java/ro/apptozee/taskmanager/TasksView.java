package ro.apptozee.taskmanager;

import ro.apptozee.taskmanager.views.FIFOTasksView;
import ro.apptozee.taskmanager.views.PIDTasksView;
import ro.apptozee.taskmanager.views.PriorityTasksView;
import ro.apptozee.taskmanager.vo.SortCriteria;
import ro.apptozee.taskmanager.vo.SortOrder;
import ro.apptozee.taskmanager.vo.Task;

import java.util.function.Consumer;

public interface TasksView {
    void list(Consumer<Task> consumer);

    // the use of the concrete class is important since this is coupled with a particular implementation
    static TasksView fromTaskManager(BaseTaskManager baseTaskManager, SortCriteria sortCriteria, SortOrder sortOrder){
        return switch (sortCriteria){
            case FIFO -> new FIFOTasksView(baseTaskManager.orderedFIFOView(),sortOrder);
            case PID -> new PIDTasksView(baseTaskManager.orderedPIDView(),sortOrder);
            case PRIORITY -> new PriorityTasksView(baseTaskManager.orderedPriorityView(),sortOrder);
            default -> throw new UnsupportedOperationException(sortCriteria+ " not implemented");
        };
    }
}
