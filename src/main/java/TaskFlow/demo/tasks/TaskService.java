package TaskFlow.demo.tasks;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import TaskFlow.demo.Mapper;
import TaskFlow.demo.projects.ProjectRepository;
import TaskFlow.demo.users.UserRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class TaskService {
    
    private final TaskRepository repository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final Mapper<Task, TaskEntity> mapper;

    public TaskService(
        TaskRepository repository,
        ProjectRepository projectRepository,
        UserRepository userRepository,
        @Qualifier("taskMapper") Mapper<Task, TaskEntity> mapper
    ) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public Task createTask(Task task) {
        var entity = mapper.toEntity(task);
        var user = userRepository.findById(task.userId()).orElseThrow(() -> new EntityNotFoundException("Not found user by id: " + task.userId()));
        var project = projectRepository.findById(task.projectId()).orElseThrow(() -> new EntityNotFoundException("Not found user by id: " + task.projectId()));

        entity.setStatus(TaskStatus.TODO);
        entity.setUser(user);
        entity.setProject(project);

        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    public Task updateStatus(Long id, TaskStatus status) {
        var task = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found task by id: " + id));

        if (!task.getStatus().canTransitTo(status)) {
            throw new IllegalArgumentException(String.format("Can't transit from %s to %s", task.getStatus(), status));
        }

        task.setStatus(status);

        var saved = repository.save(task);
        return mapper.toDomain(saved);
    }

    public Task assignExecutor(Long taskId, Long userId) {
        var task = repository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Not found task by id: " + taskId));
        var user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Not found user by id: " + userId));

        task.setUser(user);
        return mapper.toDomain(task);
    }
    
    public Task getTaskById(Long id) {
        var task = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found task by id: " + id));
        return mapper.toDomain(task);
    }
}
