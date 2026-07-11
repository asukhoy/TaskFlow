package TaskFlow.demo.tasks;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE;

    public boolean canTransitTo(TaskStatus next) {
        if (this == TODO && next == DONE) {
            return false;
        }
        return true;
    }
}
