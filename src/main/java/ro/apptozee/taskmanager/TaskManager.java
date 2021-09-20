package ro.apptozee.taskmanager;

import ro.apptozee.taskmanager.vo.*;

import java.util.Optional;
import java.util.function.Consumer;

public interface TaskManager {

    Optional<Task> add(Priority priority);

    void kill(Task task);

    void killAll();

    void killByPriority(Priority priority);

    default void list(){
         list(System.out::println);
    }

    default void list(Consumer<Task> consumer){
         list(consumer,SortCriteria.FIFO);
    }

    default void list(Consumer<Task> consumer, SortCriteria sortCriteria){
         list(consumer, sortCriteria, SortOrder.ASCENDING);
    }

    void list(Consumer<Task> consumer, SortCriteria sortCriteria, SortOrder sortOrder);

    static TaskManager withStrategy(Strategy strategy, int capacity){
        return switch (strategy){
            case BLOCK -> new BaseTaskManager(capacity, new PIDPool());
            case FIFO -> new FavorNewTaskManager(capacity, new PIDPool());
            case PRIORITY -> new PriorityTaskManager(capacity, new PIDPool());
            default -> throw new UnsupportedOperationException(strategy+ " not implemented");
        };
    }

}
