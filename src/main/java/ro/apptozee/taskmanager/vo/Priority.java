package ro.apptozee.taskmanager.vo;

import java.util.Comparator;

public enum Priority  {
    LOW(1),
    MEDIUM(10),
    HIGH(20);

    private static final Comparator<Priority> COMPARATOR = Comparator.comparing(Priority::getNumericalPriority);

    private final int numericalPriority;

    Priority(int numericalPriority) {
        this.numericalPriority = numericalPriority;
    }

    public int getNumericalPriority() {
        return numericalPriority;
    }




}
