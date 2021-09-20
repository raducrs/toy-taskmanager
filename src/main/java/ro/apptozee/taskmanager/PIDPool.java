package ro.apptozee.taskmanager;

import ro.apptozee.taskmanager.vo.PID;

import java.util.concurrent.atomic.AtomicInteger;

/*
  if using the module system this class can be marked public and not exported by the module (package private for now)
  Should be an interface implementation, but to keep this exercise short it is a concrete class
 */
class PIDPool {
    // we are only using it with mutual exclusion -> no need for AtomicInteger
    private int nextPID = 0;

    public PID getPID() {
        if(nextPID == Integer.MAX_VALUE){
            throw new PIDPoolFullException();
        }
        return new PID(nextPID++);
    }

    public void releasePID(PID pid){
        // as this was not required by the exercise I have not implemented this method
        // in a real scenario the PID would be a limited resource and would need to be reused
    }
}
