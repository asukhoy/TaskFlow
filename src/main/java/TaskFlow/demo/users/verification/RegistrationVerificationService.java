package TaskFlow.demo.users.verification;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import TaskFlow.demo.users.UserRepository;

@Service
public class RegistrationVerificationService {
    //private static final Logger log = LoggerFactory.getLogger(RegistrationVerificationService.class);

    private final UserRepository repository;

    public RegistrationVerificationService(UserRepository repository) {
        this.repository = repository;
    }

    public boolean isRegistrationAvailable(String email) {
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must contain @.");
        }
        var user = repository.findByEmail(email);
        if (user == null) {
            return true;
        }
        return false;
    }
}
