package TaskFlow.demo.users.verification;

import jakarta.validation.constraints.NotNull;

public record RegistrationVerificationRequest (
    @NotNull
    String email
) {}
