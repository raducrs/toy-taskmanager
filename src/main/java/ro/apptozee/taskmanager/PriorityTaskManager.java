package ro.apptozee.taskmanager;

import ro.apptozee.taskmanager.vo.Task;
import ro.apptozee.taskmanager.vo.Priority;

import java.util.Optional;

public class PriorityTaskManager extends BaseTaskManager{

    public PriorityTaskManager(int capacity, PIDPool pidPool) {
        super(capacity, pidPool);
    }

    @Override
    public Optional<Task> add(Priority priority) {
        rl.writeLock().lock();
        try {
            if (isFull()) {
                Optional<Task> toRemoveOpt = byPriority.entrySet().stream()
                        .filter(kv-> kv.getKey().getNumericalPriority() < priority.getNumericalPriority())
                        .filter(kv-> kv.getValue().size()>0)
                        .map(kv-> kv.getValue().entrySet().iterator().next().getValue())
                        .findFirst();

                if (toRemoveOpt.isEmpty()){
                    return Optional.empty();
                } else {
                    // we are using reentrant locks so it is safe
                    kill(toRemoveOpt.get());
                    // can add
                    return  addInternally(priority);
                }
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
