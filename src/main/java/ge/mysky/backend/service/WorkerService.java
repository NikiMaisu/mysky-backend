package ge.mysky.backend.service;

import ge.mysky.backend.domain.Role;
import ge.mysky.backend.domain.User;
import ge.mysky.backend.dto.WorkerCreateRequest;
import ge.mysky.backend.dto.WorkerUpdateRequest;
import ge.mysky.backend.repository.UserRepository;
import ge.mysky.backend.web.ConflictException;
import ge.mysky.backend.web.NotFoundException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class WorkerService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public WorkerService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> list() {
        return users.findAllByRoleAndActiveTrueOrderByNameAsc(Role.WORKER);
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        return users.findByIdAndRole(id, Role.WORKER)
                .orElseThrow(() -> new NotFoundException("Worker " + id + " not found"));
    }

    @Transactional
    public User create(WorkerCreateRequest req) {
        var email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A user with email " + email + " already exists");
        }
        var worker = User.builder()
                .name(req.name().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(Role.WORKER)
                .active(true)
                .build();
        return users.save(worker);
    }

    @Transactional
    public User update(Long id, WorkerUpdateRequest req) {
        var worker = get(id);
        var email = req.email().trim().toLowerCase();
        if (!email.equalsIgnoreCase(worker.getEmail()) && users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A user with email " + email + " already exists");
        }
        worker.setName(req.name().trim());
        worker.setEmail(email);
        if (StringUtils.hasText(req.password())) {
            worker.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        return users.save(worker);
    }

    @Transactional
    public void delete(Long id) {
        var worker = get(id);
        worker.setActive(false);
        users.save(worker);
    }
}
