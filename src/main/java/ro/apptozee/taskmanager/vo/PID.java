package ro.apptozee.taskmanager.vo;

public record PID(int pid) implements Comparable<PID> {

    @Override
    public int compareTo(PID o) {
        return Integer.compare(this.pid,o.pid);
    }

}
