package ro.apptozee.taskmanager;

import ro.apptozee.taskmanager.vo.PID;

import java.util.concurrent.atomic.AtomicInteger;

/*
  if using the module system this class can be marked public and not exported by the module (package private for now)
  Should be an interface implementation, but to keep this exercise short it is a concrete class
 */
class PIDPool {
    private final AtomicInteger nextPID = new AtomicInteger(0);

    public PID getPID() {
        // TODO check overflow
        return new PID(nextPID.getAndAdd(1));
    }

    public void releasePID(PID pid){
        // as this was not required by the exercise I have not implemented this method
        // in a real scenario the PID would be a limited resource and would need to be reused
    }
}
