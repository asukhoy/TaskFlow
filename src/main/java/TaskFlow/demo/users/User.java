package TaskFlow.demo.users;

public record User(
    Long id,
    String name,
    String email,
    String password,
    UserRole role
) {}
