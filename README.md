# How to compile instructions

You need to have a Java JDK 16+ compatible distribution installed and Maven 3.x. 
The environment variable `JAVA_HOME` should point to the jdk installation path

From inside the root directory of the project issue the command `mvn clean package`

This command will compile the code, run the tests and package the application as a `jar`

# General remarks 

This is a classical writers-readers multithreaded engineering problem and optimizations are 
very use case specific (and these optimizations are very hard to actually test in practice). As with most classes
of concurrent algorithms, we have tested for single threaded use case and we use the guarantees offered by the concurrent
paradigms to ensure that the invariants are still valid when multiple threads are competing for access. 

### Mutual exclusion of writers

Access to methods that modify the state (`add`,`kill`,etc) need to be exclusive in their totality. However we can split the `killAll` or `killByPriority` action from a bulk atomic operation to individual independent `kill` tasks, that would in theory
let the Task Manager accept new tasks as fast as more and more individual tasks are killed, but we would not be able to guarantee
a happen-before ordering of the operations

Assuming we have tasks in the queue and our individual kill routines get executed just for `n` out of them and the rest of 
them get scheduled later (potentially are forever delayed from scheduling) and in the meantime `add` methods get scheduled.
We use as example the base case.

```
Thread 1    Thread 2
killAll
    | kill(1)
    | kill(2)
    .
    | kill(n) 
            add(1)
            add(2)
            .
            add(n)
            add(n+1) -> gets rejected
```

There is no linearized (single threaded) unrolling of executions that could create this result.

Similar examples can be created for the priority implementations as well: rejecting a task since
we have removed first the much lower priority tasks. 

For the FIFO eviction strategy, splitting the `killAll` command does not cause this problem, but applying the same 
pattern to `killByPriority` could lead to the eviction of the head unnecessarily (which should not have happened before
all task of the given priority were killed)

In some applications such a requirement could not be needed and we would be fine with not having a logical ordering of operations.

### Optimistic writers access

We can envision some "optimizations" to allow multiple writers, but this is most of the time a bad idea in my experience.
Locking on individual queues based on priority would be an example (and acquiring locks in order when needed) and 
optimistically hoping we will most of the time not require the complete synchronization. This assumption can be easily broken
by use cases in which a single priority is used (but now we have the overhead of our "optimizations")

### Locks

We have used locks with fait policy instead of the synchronization on monitor objects since we do not want to cause a
potential thread starvation. It also favors writers, since we suppose it is more important to the functional requirements of
a task manager opposed to offering views to readers. The need for a reader lock is to avoid `ConcurrentModificationException`
(below we offer some alternatives that do not block readers)

### Immutability

One option to not block readers is to give them an immutable object which represents the state at a given time.

To create this immutable snapshot our writers must never modify the state, but create a fresh copy each time they modify the state
(also called `CopyOnWrite`). The caveat is that a writers will perform additional work (at least O(n)) and we create many objects (that
need to be garbage collected by the JVM and not leaked). A skeleton implementation:
```java
class TM {
    // it is seen by all threads after change
    private volatile Storage storage;
    
    public void add(Task t){
        synchronized (this){
            storage = storage.add(t);
        }
    }
    ...
    public void list(Consumer<Task> consumer){
        // non-blocking 
        storage.list(consumer);
    }
}
class Storage {
    public Storage add(Task t){
        var copy = data.copy();
        copy.add(t);
        return new Storage(data);
    }
    ...
}

```

### Concurrent data structures

We could use concurrent data structures, but there are a few caveats.

If we use multiple of them we still need to synchronize. All changes need to be executed atomically, otherwise a reader could obtain
views which are inconsistent (when only a part of data structures would have been updated). 

If we need to use only one, then different views by sorting criteria would need to perform additional work (even a full sort of data)
which defeats the purpose of highly available readers.

These data structures provide weak guarantees when iterated. One particular issue is that they could lead to unbounded 
iterators. If we use a concurrent linked list implementation we could avoid this by using a descendingIterator but this
obviously offers staled data as we can not see what was added in the meantime.
            