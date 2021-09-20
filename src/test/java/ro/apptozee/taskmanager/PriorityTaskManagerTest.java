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

class PriorityTaskManagerTest {

    private static final int CAPACITY = 5;

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        // We are not mocking PIDPool as the mock itself would mimic the real object
        PIDPool pidPool = new PIDPool();

        // given a task manager with capacity 5
        taskManager = new PriorityTaskManager(CAPACITY, pidPool);
    }

    @Test
    public void canAddWhenNotFull(){
        // given a task manager that is not at full capacity
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);

        // when adding a new task
        taskManager.add(Priority.HIGH);

        // then the new task should have been added and none of the previous tasks evicted
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
    public void canAddWhenFullIfLowerPriorityTaskExists(){
        // given a task manager that is at full capacity
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.HIGH);

        // when adding a new task with a priority higher than one of the tasks
        taskManager.add(Priority.HIGH);

        // then the new task should have been added and  the task with the lowest priority that is the oldest evicted
        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(0), Priority.HIGH,taskManager),
                        new Task(new PID(1), Priority.MEDIUM,taskManager),
                        new Task(new PID(3), Priority.LOW,taskManager),
                        new Task(new PID(4), Priority.HIGH,taskManager),
                        new Task(new PID(5), Priority.HIGH,taskManager)
                ));
    }

    @Test
    public void canNotAddWhenFullIfLowerPriorityTaskDoesNotExist(){
        // given a task manager that is at full capacity
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        // when adding a new task with a priority higher than or equal to all the tasks
        var failedAdd = taskManager.add(Priority.MEDIUM);

        // then the new task should not have been added and no task evicted
        assertTrue(failedAdd.isEmpty());

        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(0), Priority.HIGH,taskManager),
                        new Task(new PID(1), Priority.MEDIUM,taskManager),
                        new Task(new PID(2), Priority.HIGH,taskManager),
                        new Task(new PID(3), Priority.MEDIUM,taskManager),
                        new Task(new PID(4), Priority.HIGH,taskManager)
                ));
    }

}