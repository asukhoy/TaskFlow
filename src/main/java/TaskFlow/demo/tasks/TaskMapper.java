package TaskFlow.demo.tasks;

import org.springframework.stereotype.Component;

import TaskFlow.demo.Mapper;

@Component
public class TaskMapper implements Mapper<Task, TaskEntity> {
    public Task toDomain(TaskEntity task) {
        return new Task(
            task.getId(),
            task.getName(),
            task.getDescription(),
            task.getStatus(),
            task.getProject().getId(),
            task.getUser().getId()
        );
    }

    public TaskEntity toEntity(Task task) {
        return new TaskEntity(
            task.id(),
            task.name(),
            task.description(),
            task.status()
        );
    }
}
