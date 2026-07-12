package TaskFlow.demo.users;

import org.springframework.stereotype.Component;

import TaskFlow.demo.Mapper;

@Component
public class UserMapper implements Mapper<User, UserEntity> {
    public User toDomain(UserEntity user) {
        return new User(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPassword(),
            user.getRole()
        );
    }

    public UserEntity toEntity(User user) {
        return new UserEntity(
            user.id(),
            user.name(),
            user.email(),
            user.password(),
            user.role()
        );
    }
}
