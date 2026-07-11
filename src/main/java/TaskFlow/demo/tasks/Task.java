package TaskFlow.demo.tasks;

public record Task(
    Long id,
    String name,
    String description,
    TaskStatus status,
    Long projectId,
    Long userId
) {}
