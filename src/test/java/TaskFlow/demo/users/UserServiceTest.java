package TaskFlow.demo.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import TaskFlow.demo.users.verification.RegistrationVerificationService;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RegistrationVerificationService verificationService;

    @InjectMocks
    private UserService userService;

    private UserEntity userEntity;
    private User userDto;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("developer@taskflow.com");
        userEntity.setRole(UserRole.USER);

        userDto = new User(
            1L,
            "A",
            "developer@taskflow.com",
            "123",
            UserRole.ADMIN
        );
    }

    @Test
    @DisplayName("Успешный поиск пользователя по ID")
    void getUserById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDomain(userEntity)).thenReturn(userDto);

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("developer@taskflow.com", result.email());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Поиск пользователя с несуществующим ID")
    void getUserById_ShouldThrow() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {userService.getUserById(2L);});

        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Выброс исключения, если пользователь с таким Email уже существует")
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        when(verificationService.isRegistrationAvailable(userDto.email())).thenReturn(false);

        var exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(userDto);
        });

        assertEquals("User with this email adress: " + userDto.email() + " exists.", exception.getMessage());

        verifyNoInteractions(userMapper);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void registerUser_Success() {
        User inputUser = new User(null, "B", "test@email.com", "PlainPassword", UserRole.ADMIN);
        UserEntity userEntity = new UserEntity(null, "B", "test@email.com", "PlainPassword", UserRole.ADMIN);
        UserEntity savedEntity = new UserEntity(1L, "B", "test@email.com", "PlainPassword", UserRole.ADMIN);
        User expectedUser = new User(1L, "B", "test@email.com", "PlainPassword", UserRole.ADMIN);

        when(verificationService.isRegistrationAvailable(inputUser.email())).thenReturn(true);
        when(userMapper.toEntity(inputUser)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(savedEntity);
        when(userMapper.toDomain(savedEntity)).thenReturn(expectedUser);

        User result = userService.registerUser(inputUser);

        assertNotNull(result);
        assertEquals(expectedUser.id(), result.id());
        assertEquals(expectedUser.email(), result.email());

        verify(verificationService, times(1)).isRegistrationAvailable(inputUser.email());
        verify(userMapper, times(1)).toEntity(inputUser);
        verify(userRepository, times(1)).save(userEntity);
        verify(userMapper, times(1)).toDomain(savedEntity);
    }

    @Test
    @DisplayName("Ввод некорректного email")
    void rigisterUser_IncorrectEmail() {
        User inputUser = new User(null, "B", "testemail.com", "PlainPassword", UserRole.ADMIN);

        when(verificationService.isRegistrationAvailable(inputUser.email())).thenThrow(new IllegalArgumentException("Email must contain @."));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        userService.registerUser(inputUser);});

        assertEquals("Email must contain @.", exception.getMessage());

        verify(verificationService, times(1)).isRegistrationAvailable(inputUser.email());
    
        verifyNoInteractions(userMapper);
        verifyNoInteractions(userRepository);
    }
}