package TaskFlow.demo.projects;

import java.util.ArrayList;
import java.util.List;

import TaskFlow.demo.tasks.TaskEntity;
import TaskFlow.demo.users.UserEntity;
import jakarta.persistence.*;

@Table(name = "projects")
@Entity
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_project_user"))
    private UserEntity user;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<TaskEntity> tasks = new ArrayList<>();

    public ProjectEntity() {}

    public ProjectEntity(
        Long id,
        String name,
        String description
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setUser(UserEntity user) {
        this.user = user;
        if (user != null && !user.getProjects().contains(this)) {
            user.addProject(this);
        }
    }

    public UserEntity getUser() {
        return user;
    }

    public List<TaskEntity> getTasks() {
        return tasks;
    }

    public void addTask(TaskEntity task) {
        this.tasks.add(task);
    }

    public void removeTask(TaskEntity task) {
        this.tasks.remove(task);
    }
}
