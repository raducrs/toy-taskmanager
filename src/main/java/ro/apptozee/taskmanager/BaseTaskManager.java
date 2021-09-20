package ro.apptozee.taskmanager;

import ro.apptozee.taskmanager.vo.*;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;


public class BaseTaskManager implements TaskManager {

    private final int capacity;
    private volatile int size;

    protected final PIDPool pidPool;

    protected final LinkedHashMap<PID, Task> queue = new LinkedHashMap<>();

    // The added benefit of these data structure is debatable since sorting the queue based on priority using
    // a bucket sorting algorithm is still O(n) runtime when creating the view, but with 2 traversals and
    // would require O(n) space for each individual request

    // Likewise, when used by killByPriority the worst case running time is still O(n) which is no different from
    // traversal of the queue and checking the priority of each individual task
    protected final TreeMap<Priority, Map<PID,Task>> byPriority = new TreeMap<>(Priority.BY_PRIORITY);

    // Since we are in control of creating the tasks we know that the insertion order of PID is not very different
    // from the insertion order of FIFO (the FIFO order is almost PID sorted)
    // In this case we could implement a version of sorting based on insertion sort that looks on average only k cells apart with
    // a runtime of O(kn) where n is the list length (k<<n)
    // For the scope of this exercise we will use this extra data structure,
    // although it is not needed since FIFO and PID order are exactly the same, but we left it to show how to handle another sortCriteria
    protected final TreeSet<Task> byPID = new TreeSet<>(Task.BY_PID_COMP);

    // we could make it stamped lock if we can guarantee increased performance, but let's not optimize early
    ReadWriteLock rl = new ReentrantReadWriteLock(true);

    public BaseTaskManager(int capacity, PIDPool pidPool) {
        if (capacity < 1){
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.pidPool = pidPool;
    }


    @Override
    public Optional<Task> add(Priority priority) {
        rl.writeLock().lock();
        try {
            if (isFull()){
                return Optional.empty();
            }

            // atomic modification of all structures guaranteed by the read-write lock
            return addInternally(priority);
        } finally {
            rl.writeLock().unlock();
        }
    }

    protected Optional<Task> addInternally(Priority priority){
        Task task = null;
        try {
            task = new Task(pidPool.getPID(), priority, this);
        } catch (PIDPool.PIDPoolFullException ex){
            // log here
            return Optional.empty();
        }
        queue.put(task.pid(),task);
        byPriority.computeIfAbsent(task.priority(),(p) -> new LinkedHashMap<>());
        byPriority.get(task.priority()).put(task.pid(),task);
        byPID.add(task);
        size++;
        return Optional.of(task);
    }

    protected boolean isFull(){
        return size == capacity;
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
            for(var task: tasksToRemove){
                // a bulk remove is o(n) while calling the kill method repeatedly is o(n lg(n)) because of the TreeSet
                // kill(task);
                queue.remove(task.pid());
                task.kill();
                pidPool.releasePID(task.pid());
            }
            byPID.clear();
            byPriority.forEach((p,t)->t.clear());
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
                // we can not use bulk remove here and we will have O(lg(n)) cost per iteration
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
            // moved out responsibility of views from task manager (the classes are still tightly coupled however)
            TasksView.fromTaskManager(this,sortCriteria,sortOrder).list(consumer);
        }
        finally {
            rl.readLock().unlock();
        }

    }

     // package private methods to be accessible only TasksView
     LinkedHashMap<PID, Task> orderedFIFOView() {
        return queue;
    }

    TreeSet<Task> orderedPIDView() {
        return byPID;
    }

    TreeMap<Priority, Map<PID, Task>> orderedPriorityView() {
        return byPriority;
    }
}
