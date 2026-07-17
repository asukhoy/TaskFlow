package TaskFlow.demo.projects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import TaskFlow.demo.users.UserEntity;
import TaskFlow.demo.users.UserRepository;
import TaskFlow.demo.users.UserRole;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
    @Mock
    private ProjectRepository repository;

    @Mock
    private ProjectMapper mapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService service;

    private Project project;
    private UserEntity user;
    private ProjectEntity projectEntity;

    @BeforeEach
    void setUp() {
        project = new Project(
            null,
            "A",
            null,
            1L
        );

        user = new UserEntity(
            1L,
            "A",
            "A",
            "A",
            UserRole.ADMIN
        );

        projectEntity = new ProjectEntity(
            null,
            "A",
            null
        );
    }

    @Test
    @DisplayName("Удачное создание проекта")
    void createProject_Success() {
        var projectEntityAfter = new ProjectEntity(
            1L,
            "A",
            null
        );

        projectEntityAfter.setUser(user);

        var projectAfter = new Project(
            1L,
            "A",
            null,
            1L
        );

        Optional<UserEntity> opt = Optional.of(user);

        when(mapper.toEntity(project)).thenReturn(projectEntity);
        when(userRepository.findById(project.userId())).thenReturn(opt);
        when(repository.save(any(ProjectEntity.class))).thenReturn(projectEntityAfter);
        when(mapper.toDomain(projectEntityAfter)).thenReturn(projectAfter);

        Project result = service.createProject(project);

        assertNotNull(result);
        assertEquals(projectAfter.id(), result.id());
        assertEquals(projectAfter.userId(), result.userId());

        verify(mapper).toEntity(project);
        verify(mapper).toDomain(projectEntityAfter);
        verify(userRepository).findById(project.userId());
        verify(repository).save(projectEntity);
    }

    @Test
    @DisplayName("Создание проекта несуществующим пользователем")
    void createProject_NotRegisteredUser() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(EntityNotFoundException.class, () -> service.createProject(project));

        assertEquals("Not found user by id: 1", exception.getMessage());

        verify(userRepository).findById(1L);
        verify(mapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("Создание проекта обычным пользователем")
    void createProject_NotAllowed() {

        user.setRole(UserRole.USER);

        Optional<UserEntity> opt = Optional.of(user);
        when(userRepository.findById(1L)).thenReturn(opt);

        var exception = assertThrows(IllegalAccessError.class, () -> service.createProject(project));

        assertEquals("User must be admin", exception.getMessage());

        verify(userRepository).findById(1L);
        verify(mapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("Успешное получение проекта по id")
    void getProjectById_Success() {
        var project1 = new Project(
            1L,
            "A",
            null,
            1L
        );

        projectEntity.setUser(user);

        when(repository.findById(projectEntity.getId())).thenReturn(Optional.of(projectEntity));
        when(mapper.toDomain(projectEntity)).thenReturn(project1);

        Project res = service.getProjectById(projectEntity.getId());

        assertNotNull(res);
        assertEquals(project1.id(), res.id());
        assertEquals(project1.name(), res.name());
        assertEquals(project1.description(), res.description());
        assertEquals(project1.userId(), res.userId());

        verify(mapper).toDomain(projectEntity);
        verify(repository).findById(projectEntity.getId());
    }

    @Test
    @DisplayName("Попытка получить несуществующий проект")
    void getProjectById_Fail() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(EntityNotFoundException.class, () -> service.getProjectById(1L));

        assertEquals("Project with id: 1 not found", exception.getMessage());

        verify(repository).findById(1L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Корректная смена владельца")
    void changeOwner_Success() {
        projectEntity.setId(1L);
        var ownerBefore = new UserEntity(
            2L,
            "B",
            "B",
            "B",
            UserRole.ADMIN
        );

        projectEntity.setUser(ownerBefore);

        var projectAfter = new Project(
            1L,
            "A",
            null,
            1L
        );

        var projectEntityAfter = new ProjectEntity(
            1L,
            "A",
            null
        );

        when(repository.findById(projectEntity.getId())).thenReturn(Optional.of(projectEntity));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        projectEntityAfter.setUser(user);

        when(repository.save(any(ProjectEntity.class))).thenReturn(projectEntityAfter);
        when(mapper.toDomain(projectEntityAfter)).thenReturn(projectAfter);

        assertNotEquals(projectEntity.getUser().getId(), projectEntityAfter.getUser().getId());

        var res = service.changeOwner(projectEntity.getId(), user.getId());

        assertNotNull(res);
        assertEquals(projectAfter, res);

        verify(repository).findById(projectEntity.getId());
        verify(repository).save(projectEntity);
        verify(userRepository).findById(user.getId());
        verify(mapper).toDomain(projectEntityAfter);
    }

    @Test
    @DisplayName("Смена владельца несуществующего проекта")
    void changeOwner_IncorrectProjectId() {
        projectEntity.setId(1L);

        when(repository.findById(projectEntity.getId())).thenReturn(Optional.empty());

        var exception = assertThrows(EntityNotFoundException.class, () -> service.changeOwner(projectEntity.getId(), 1L));

        assertEquals("Project with id: " + projectEntity.getId() + " not found", exception.getMessage());

        verify(repository).findById(projectEntity.getId());
        verify(userRepository, never()).findById(1L);
    }

    @Test
    @DisplayName("Попытка установить несуществующего владельца")
    void changeOwner_IncorrectUserId() {
        projectEntity.setId(1L);
        when(repository.findById(projectEntity.getId())).thenReturn(Optional.of(projectEntity));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(EntityNotFoundException.class, () -> service.changeOwner(projectEntity.getId(), 1L));

        assertEquals("User with id: 1 not found", exception.getMessage());

        verify(repository).findById(projectEntity.getId());
        verify(userRepository).findById(1L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Попытка установить обычного пользователя владельцем")
    void changeOwner_InccorectUserRole() {
        projectEntity.setId(1L);
        user.setRole(UserRole.USER);

        when(repository.findById(projectEntity.getId())).thenReturn(Optional.of(projectEntity));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        var exception = assertThrows(IllegalAccessError.class, () -> service.changeOwner(projectEntity.getId(), user.getId()));

        assertEquals("User must be admin", exception.getMessage());

        verify(repository).findById(projectEntity.getId());
        verify(userRepository).findById(user.getId());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Успешное удаление проекта")
    void deleteProject_Success() {
        when(repository.existsById(1L)).thenReturn(true);

        var res = service.deleteProject(1L);

        assertEquals(true, res);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Неудачное удаление проекта")
    void deleteProject_Fail() {
        when(repository.existsById(1L)).thenReturn(false);

        var res = service.deleteProject(1L);

        assertEquals(false, res);

        verify(repository).existsById(1L);
        verify(repository, never()).deleteById(1L);
    }
}
