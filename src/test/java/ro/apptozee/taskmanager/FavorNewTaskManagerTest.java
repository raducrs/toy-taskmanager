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

class FavorNewTaskManagerTest {

    private static final int CAPACITY = 5;

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        // We are not mocking PIDPool as the mock itself would mimic the real object
        PIDPool pidPool = new PIDPool();

        // given a task manager with capacity 5
        taskManager = new FavorNewTaskManager(CAPACITY, pidPool);
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
    public void canAddWhenFull(){
        // given a task manager that is at full capacity
        taskManager.add(Priority.HIGH);
        taskManager.add(Priority.LOW);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.MEDIUM);
        taskManager.add(Priority.HIGH);

        // when adding a new task
        taskManager.add(Priority.HIGH);

        // then the new task should have been added and the oldest tasks evicted
        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(1), Priority.LOW,taskManager),
                        new Task(new PID(2), Priority.MEDIUM,taskManager),
                        new Task(new PID(3), Priority.MEDIUM,taskManager),
                        new Task(new PID(4), Priority.HIGH,taskManager),
                        new Task(new PID(5), Priority.HIGH,taskManager)
                ));
    }


    @Test
    public void canAddWhenCapacity1(){
        // given a task manager with a capacity of 1 that is at full capacity
        taskManager = new FavorNewTaskManager(1,new PIDPool());
        taskManager.add(Priority.HIGH);

        // when adding a new task
        taskManager.add(Priority.LOW);

        // then the new task should have been added and the oldest tasks evicted
        var insertionOrder = new LinkedList<>();
        taskManager.list(insertionOrder::add);
        assertThat(insertionOrder)
                .isEqualTo(Arrays.asList(
                        new Task(new PID(1), Priority.LOW,taskManager)
                ));
    }
}