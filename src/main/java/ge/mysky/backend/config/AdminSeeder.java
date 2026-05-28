package ge.mysky.backend.config;

import ge.mysky.backend.domain.Role;
import ge.mysky.backend.domain.User;
import ge.mysky.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final MyskyProperties props;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder, MyskyProperties props) {
        this.users = users;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    public void run(String... args) {
        if (users.existsByRole(Role.ADMIN)) {
            log.info("Admin user already exists, skipping seed.");
            return;
        }

        var admin = props.admin();
        var user = User.builder()
                .name(admin.name())
                .email(admin.email().toLowerCase())
                .passwordHash(encoder.encode(admin.password()))
                .role(Role.ADMIN)
                .build();
        users.save(user);

        log.warn("Seeded initial admin user: {} (password from MYSKY_ADMIN_PASSWORD or default).", admin.email());
        log.warn("Change the password via your admin UI before exposing this instance.");
    }
}
