package ro.apptozee.taskmanager;

import ro.apptozee.taskmanager.vo.Task;
import ro.apptozee.taskmanager.vo.Priority;

import java.util.Optional;

public class FavorNewTaskManager extends BaseTaskManager{

    public FavorNewTaskManager(int capacity, PIDPool pidPool) {
        super(capacity, pidPool);
    }

    @Override
    public Optional<Task> add(Priority priority) {
        rl.writeLock().lock();
        try {
            if (isFull()) {
                // we are guaranteed we can find an element if we have a capacity of more than 1
                var taskToRemove = queue.entrySet().iterator().next().getValue();
                // we are using reentrant locks so it is safe
                kill(taskToRemove);

                return addInternally(priority);
            } else {
                //we have room we, so we can simply add it without worrying about eviction
                return  addInternally(priority);
            }
        }
        finally {
            rl.writeLock().unlock();
        }
        // if we are operating in full capacity

    }
}
