package ge.mysky.backend.repository;

import ge.mysky.backend.domain.Role;
import ge.mysky.backend.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByRole(Role role);
}
