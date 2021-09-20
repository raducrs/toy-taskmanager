package ro.apptozee.taskmanager;

public class PIDPoolFullException extends RuntimeException{
    public PIDPoolFullException() {
        super("PID Pool Full");
    }
}
