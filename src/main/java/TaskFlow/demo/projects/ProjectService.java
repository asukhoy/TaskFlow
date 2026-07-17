package TaskFlow.demo.projects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import TaskFlow.demo.Mapper;
import TaskFlow.demo.users.UserRepository;
import TaskFlow.demo.users.UserRole;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ProjectService {
    
    private final ProjectRepository repository;
    private final Mapper<Project, ProjectEntity> mapper;
    private final UserRepository userRepository;

    public ProjectService(
        ProjectRepository repository,
        @Qualifier("projectMapper") Mapper<Project, ProjectEntity> mapper,
        UserRepository userRepository
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    public Project createProject(Project project) {
        var user = userRepository.findById(project.userId()).orElseThrow(() -> new EntityNotFoundException("Not found user by id: " + project.userId()));
        if (user.getRole() != UserRole.ADMIN) {
            throw new IllegalAccessError("User must be admin");
        }
        var entity = mapper.toEntity(project);
        entity.setUser(user);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    public Project changeOwner(Long projectId, Long userId) {
        var projectEntity = repository.findById(projectId).orElseThrow(() -> new EntityNotFoundException("Project with id: " + projectId + " not found"));
        var userEntity = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " not found"));
        if (userEntity.getRole() != UserRole.ADMIN) {
            throw new IllegalAccessError("User must be admin");
        }
        projectEntity.setUser(userEntity);
        var saved = repository.save(projectEntity);
        return mapper.toDomain(saved);
    }

    public Project getProjectById(Long id) {
        var projectEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Project with id: " + id + " not found"));
        return mapper.toDomain(projectEntity);
    }

    public boolean deleteProject(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
