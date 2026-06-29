package ge.mysky.backend.service;

import ge.mysky.backend.domain.Role;
import ge.mysky.backend.domain.User;
import ge.mysky.backend.dto.WorkerCreateRequest;
import ge.mysky.backend.dto.WorkerUpdateRequest;
import ge.mysky.backend.repository.UserRepository;
import ge.mysky.backend.web.ConflictException;
import ge.mysky.backend.web.NotFoundException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

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
        var email = StringUtils.hasText(req.email()) ? req.email().trim().toLowerCase() : null;
        var phone = StringUtils.hasText(req.phone()) ? req.phone().trim() : null;
        if (email == null && phone == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email or phone is required");
        }
        if (email != null && users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A user with email " + email + " already exists");
        }
        if (phone != null && users.existsByPhone(phone)) {
            throw new ConflictException("A user with phone " + phone + " already exists");
        }
        var worker = User.builder()
                .name(req.name().trim())
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(Role.WORKER)
                .active(true)
                .build();
        return users.save(worker);
    }

    @Transactional
    public User update(Long id, WorkerUpdateRequest req) {
        var worker = get(id);
        var email = StringUtils.hasText(req.email()) ? req.email().trim().toLowerCase() : null;
        var phone = StringUtils.hasText(req.phone()) ? req.phone().trim() : null;
        if (email == null && phone == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email or phone is required");
        }
        if (email != null && !email.equalsIgnoreCase(worker.getEmail()) && users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A user with email " + email + " already exists");
        }
        if (phone != null && !phone.equals(worker.getPhone()) && users.existsByPhone(phone)) {
            throw new ConflictException("A user with phone " + phone + " already exists");
        }
        worker.setName(req.name().trim());
        worker.setEmail(email);
        worker.setPhone(phone);
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
