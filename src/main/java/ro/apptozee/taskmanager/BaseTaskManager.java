package ro.apptozee.taskmanager;

import ro.apptozee.taskmanager.vo.*;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BaseTaskManager implements TaskManager {

    private final int capacity;
    // TODO check if volatile is needed for int variables
    private volatile int size; // TODO offer accessors to subclasses

    protected final PIDPool pidPool;

    private final LinkedHashMap<PID, Task> queue = new LinkedHashMap<>();
    private final TreeMap<Priority, Map<PID,Task>> byPriority = new TreeMap<>(Priority.BY_PRIORITY);
    private final TreeSet<Task> byPID = new TreeSet<>(Task.BY_PID_COMP);

    // we could make it stamped lock if we can guarantee increased performance, but let's not optimize early
    ReadWriteLock rl = new ReentrantReadWriteLock(true);

    public BaseTaskManager(int capacity, PIDPool pidPool) {
        this.capacity = capacity;
        this.pidPool = pidPool;
    }


    @Override
    public Optional<Task> add(Priority priority) {
        rl.writeLock().lock();
        try {
            if (size == capacity){
                return Optional.empty();
            }
            var task = new Task(pidPool.getPID(),priority,this);
            // atomic modification of all structures guaranteed by the read-write lock
            addInternally(task);
            return Optional.of(task);
        } finally {
            rl.writeLock().unlock();
        }
    }

    private void addInternally(Task task){
        queue.put(task.pid(),task);
        byPriority.computeIfAbsent(task.priority(),(p) -> new LinkedHashMap<>());
        byPriority.get(task.priority()).put(task.pid(),task);
        byPID.add(task);
        size++;
    }

    @Override
    public void kill(Task task) {
        rl.writeLock().lock();
        try {
            if (queue.containsKey(task.pid())){
                // order is important to avoid loops
                queue.remove(task.pid());
                task.kill();
                pidPool.releasePID(task.pid());
                // we are guaranteed entry exists
                byPriority.get(task.priority()).remove(task.pid());
                byPID.remove(task);
                size--;
            }
        }
        finally {
            rl.writeLock().unlock();
        }
    }

    @Override
    public void killAll() {
        rl.writeLock().lock();
        try {
            // avoid concurrent modification exception
            var tasksToRemove = new ArrayList<>(queue.values());
            // a bulk remove is o(n) while this is o(n lg(n)) because of the TreeSet
            for(var task: tasksToRemove){
                kill(task);
            }
        }
        finally {
            rl.writeLock().unlock();
        }
    }

    @Override
    public void killByPriority(Priority priority) {
        rl.writeLock().lock();
        try {
            // avoid concurrent modification exception
            var tasksToRemove = new ArrayList<>(byPriority.getOrDefault(priority,Collections.emptyMap()).values());
            // we have reentrant locks so it is okay
            for(var task: tasksToRemove){
                kill(task);
            }
        }
        finally {
            rl.writeLock().unlock();
        }
    }

    @Override
    public void list(Consumer<Task> consumer, SortCriteria sortCriteria, SortOrder sortOrder) {
        rl.readLock().lock();
        try {
            var st = switch (sortCriteria){
                case FIFO -> {
                    if (sortOrder == SortOrder.ASCENDING) {
                        yield queue.entrySet().stream().map(Map.Entry::getValue);
                    } else {
                        // can not do any better than this without accessing internal state unfortunately (since we do not have descending iterators)
                        var reversed = queue.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
                        Collections.reverse(reversed);
                        yield reversed.stream();
                    }
                }
                case PID -> {
                    if (sortOrder == SortOrder.ASCENDING) {
                        yield StreamSupport.stream(Spliterators.spliteratorUnknownSize(byPID.iterator(), 0), false);
                    } else {
                        yield StreamSupport.stream(Spliterators.spliteratorUnknownSize(byPID.descendingIterator(), 0), false);
                    }
                }
                case PRIORITY -> {
                    if (sortOrder == SortOrder.ASCENDING) {
                        yield byPriority.entrySet().stream().flatMap(kv->kv.getValue().values().stream());
                    } else {
                        yield byPriority.descendingMap().entrySet().stream().flatMap(kv->kv.getValue().values().stream());
                    }
                }
                default -> throw new UnsupportedOperationException("not implemented");
            };

           st.forEach(consumer);
        }
        finally {
            rl.readLock().unlock();
        }

    }
}
