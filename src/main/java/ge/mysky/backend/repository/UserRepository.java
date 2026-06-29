package ge.mysky.backend.repository;

import ge.mysky.backend.domain.Role;
import ge.mysky.backend.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByRole(Role role);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    List<User> findAllByRoleAndActiveTrueOrderByNameAsc(Role role);

    Optional<User> findByIdAndRole(Long id, Role role);
}
