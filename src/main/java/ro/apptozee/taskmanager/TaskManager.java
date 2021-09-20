package ro.apptozee.taskmanager;

import ro.apptozee.taskmanager.vo.Task;
import ro.apptozee.taskmanager.vo.Priority;
import ro.apptozee.taskmanager.vo.SortCriteria;
import ro.apptozee.taskmanager.vo.SortOrder;

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

}
