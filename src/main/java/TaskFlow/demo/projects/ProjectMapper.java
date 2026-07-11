package TaskFlow.demo.projects;

import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
    public Project toDomain(ProjectEntity project) {
        return new Project(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getUser().getId()
        );
    }

    public ProjectEntity toEntity(Project project) {
        return new ProjectEntity(
            project.id(),
            project.name(),
            project.description()
        );
    }
}
