package TaskFlow.demo.users;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import TaskFlow.demo.Mapper;
import TaskFlow.demo.users.verification.RegistrationVerificationService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {


    private final UserRepository repository;
    private final Mapper<User, UserEntity> mapper;
    private final RegistrationVerificationService verificationService;

    public UserService(
        UserRepository repository,
        @Qualifier("userMapper") Mapper<User, UserEntity> mapper,
        RegistrationVerificationService verificationService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.verificationService = verificationService;
    }

    public User registerUser(User user) {
        var check = verificationService.isRegistrationAvailable(user.email());
        if (!check) {
            throw new UserAlreadyExistsException("User with this email adress: " + user.email() + " exists.");
        }
        var entity = mapper.toEntity(user);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    public User getUserById(Long id) {
        var user = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found user by id: " + id));
        return mapper.toDomain(user);
    }
}
