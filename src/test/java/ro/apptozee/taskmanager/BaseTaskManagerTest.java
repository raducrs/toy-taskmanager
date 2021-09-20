package ro.apptozee.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.apptozee.taskmanager.vo.PID;
import ro.apptozee.taskmanager.vo.Priority;
import ro.apptozee.taskmanager.vo.Task;

import java.util.Arrays;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BaseTaskManagerTest {

    protected static final int CAPACITY = 5;

    protected TaskManager taskManager;

    @BeforeEach
    void setUp() {
        // We are not mocking PIDPool as the mock itself would mimic the real object
        PIDPool pidPool = new PIDPool();

        // given a task manager with capacity 5
        taskManager = new BaseTaskManager(CAPACITY, pidPool);
    }

    @Test
    public void canAddWhenCapacity(){
        // given a task manager with capacity 5

        // when adding 5 tasks
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        // then all 5 tasks should be present
        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(0), Priority.HIGH,taskManager),
                        new Task(new PID(1), Priority.LOW,taskManager),
                        new Task(new PID(2), Priority.MEDIUM,taskManager),
                        new Task(new PID(3), Priority.MEDIUM,taskManager),
                        new Task(new PID(4), Priority.HIGH,taskManager)
                ));

    }

    @Test
    public void canNotAddWhenNotCapacity(){
        // given a task manager with capacity 5

        // when adding 6 tasks
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.HIGH);

        // then only the first 5 tasks were accepted to capacity
        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(0), Priority.HIGH,taskManager),
                        new Task(new PID(1), Priority.LOW,taskManager),
                        new Task(new PID(2), Priority.MEDIUM,taskManager),
                        new Task(new PID(3), Priority.MEDIUM,taskManager),
                        new Task(new PID(4), Priority.HIGH,taskManager)
                ));

    }

    @Test
    public void canCreateCapacityByKill(){
        // given a task manager that contains a specific task
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        var toRemoveOpt = taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        // when removing that specific task
        assertFalse(toRemoveOpt.isEmpty());
        taskManager.kill(toRemoveOpt.get());

        // there is capacity to add a new task and the previous task is not present
        taskManager.add(Priority.HIGH);

        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(0), Priority.HIGH,taskManager),
                        new Task(new PID(1), Priority.LOW,taskManager),
                        new Task(new PID(3), Priority.MEDIUM,taskManager),
                        new Task(new PID(4), Priority.HIGH,taskManager),
                        new Task(new PID(5), Priority.HIGH,taskManager)
                ));

    }

    @Test
    public void canNotCreateFalseCapacityByKill(){
        // given a task manager at full capacity that contains a specific task
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        var toRemoveOpt = taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        // when removing the task twice and adding two new tasks

        // kill task with PID 2
        assertFalse(toRemoveOpt.isEmpty());
        taskManager.kill(toRemoveOpt.get());

        // adding new task
        taskManager.add(Priority.HIGH);

        // re kill task with PID 2
        taskManager.kill(toRemoveOpt.get());

        // adding a second new task
        var failedAdd = taskManager.add(Priority.LOW);


        // then the task over capacity should not be added as there is no capacity and no other task removed
        assertTrue(failedAdd.isEmpty());

        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(0), Priority.HIGH,taskManager),
                        new Task(new PID(1), Priority.LOW,taskManager),
                        new Task(new PID(3), Priority.MEDIUM,taskManager),
                        new Task(new PID(4), Priority.HIGH,taskManager),
                        new Task(new PID(5), Priority.HIGH,taskManager)
                ));

    }

    @Test
    public void canCreateCapacityByKillByPriority(){
        // when given a task manager that contains task with a priority
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        // when killing task with the given priority and adding more tasks to capacity

        // kill task with PID 2 & 3
        taskManager.killByPriority(Priority.MEDIUM);

        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        // the task that had the given priority and were killed should not be present thus
        // having created enough room for new task and no other task with a different priority were killed

        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(0), Priority.HIGH,taskManager),
                        new Task(new PID(1), Priority.LOW,taskManager),
                        new Task(new PID(4), Priority.HIGH,taskManager),
                        new Task(new PID(5), Priority.MEDIUM,taskManager),
                        new Task(new PID(6), Priority.HIGH,taskManager)

                ));

    }

    @Test
    public void canNotCreateFalseCapacityByKillByPriority(){
        // when given a task manager that contains task with a priority
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        // when issuing twice killing task with the given priority and adding more tasks over capacity

        // kill task with priority (PID 2 & 3)
        taskManager.killByPriority(Priority.MEDIUM);

        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        // re kill task with priority (PID 5)
        taskManager.killByPriority(Priority.MEDIUM);

        taskManager.add(Priority.HIGH);
        var failedAdd = taskManager.add(Priority.HIGH);

        // the task that had the given priority and were killed should not be present thus
        // having created enough room for new task whilst no other task with a different priority were killed
        // and the capacity of the task manager was not modified

        assertTrue(failedAdd.isEmpty());


        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(0), Priority.HIGH,taskManager),
                        new Task(new PID(1), Priority.LOW,taskManager),
                        new Task(new PID(4), Priority.HIGH,taskManager),
                        new Task(new PID(6), Priority.HIGH,taskManager),
                        new Task(new PID(7), Priority.HIGH,taskManager)
                ));

    }

    @Test
    public void KillAllRemovesAllTask(){
        //given a task manager with tasks
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        // when issuing kill ALL
        taskManager.killAll();


        // there should be no task left
        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder).isEmpty();
    }

    @Test
    public void canCreateCapacityByKillAll(){
        //given a task manager with tasks and running the killAll command
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        taskManager.killAll();

        // when adding tasks to capacity
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        taskManager.add(Priority.HIGH);
        var failedAdd = taskManager.add(Priority.HIGH);

        // then they should be accepted until capacity
        assertTrue(failedAdd.isEmpty());

        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(5), Priority.HIGH,taskManager),
                        new Task(new PID(6), Priority.LOW,taskManager),
                        new Task(new PID(7), Priority.MEDIUM,taskManager),
                        new Task(new PID(8), Priority.MEDIUM,taskManager),
                        new Task(new PID(9), Priority.HIGH,taskManager)
                ));

    }

    @Test
    public void canNotFCreateFalseCapacityByKillAll(){
        //given a task manager with tasks and running the killAll twice command
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        taskManager.killAll();
        taskManager.killAll();

        // when adding tasks to capacity and over
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        // then they should all be accepted

        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(5), Priority.HIGH,taskManager),
                        new Task(new PID(6), Priority.LOW,taskManager),
                        new Task(new PID(7), Priority.MEDIUM,taskManager),
                        new Task(new PID(8), Priority.MEDIUM,taskManager),
                        new Task(new PID(9), Priority.HIGH,taskManager)
                ));
    }

}