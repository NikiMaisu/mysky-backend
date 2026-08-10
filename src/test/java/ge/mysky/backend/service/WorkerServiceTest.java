package ge.mysky.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ge.mysky.backend.domain.Role;
import ge.mysky.backend.domain.User;
import ge.mysky.backend.dto.WorkerCreateRequest;
import ge.mysky.backend.dto.WorkerUpdateRequest;
import ge.mysky.backend.repository.UserRepository;
import ge.mysky.backend.web.ConflictException;
import ge.mysky.backend.web.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder passwordEncoder;

    private WorkerService service;

    @BeforeEach
    void setUp() {
        service = new WorkerService(users, passwordEncoder);
        lenient().when(passwordEncoder.encode(org.mockito.ArgumentMatchers.anyString())).thenReturn("hashed");
    }

    @Test
    void listReturnsActiveWorkersOnly() {
        var worker = User.builder().id(1L).name("Ana").role(Role.WORKER).active(true).build();
        when(users.findAllByRoleAndActiveTrueOrderByNameAsc(Role.WORKER)).thenReturn(List.of(worker));

        assertThat(service.list()).containsExactly(worker);
    }

    @Test
    void getThrowsWhenMissing() {
        when(users.findByIdAndRole(9L, Role.WORKER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(9L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void createRequiresEmailOrPhone() {
        var req = new WorkerCreateRequest("Ana", null, null, "password1");

        assertThatThrownBy(() -> service.create(req)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(users.existsByEmailIgnoreCase("ana@mysky.ge")).thenReturn(true);

        var req = new WorkerCreateRequest("Ana", "ANA@mysky.ge", null, "password1");

        assertThatThrownBy(() -> service.create(req)).isInstanceOf(ConflictException.class);
    }

    @Test
    void createRejectsDuplicatePhone() {
        when(users.existsByPhone("599112233")).thenReturn(true);

        var req = new WorkerCreateRequest("Ana", null, "599112233", "password1");

        assertThatThrownBy(() -> service.create(req)).isInstanceOf(ConflictException.class);
    }

    @Test
    void createNormalizesEmailAndTrimsPhone() {
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new WorkerCreateRequest("  Ana  ", "ANA@mysky.ge", " 599112233 ", "password1");
        var created = service.create(req);

        assertThat(created.getName()).isEqualTo("Ana");
        assertThat(created.getEmail()).isEqualTo("ana@mysky.ge");
        assertThat(created.getPhone()).isEqualTo("599112233");
        assertThat(created.getRole()).isEqualTo(Role.WORKER);
        assertThat(created.isActive()).isTrue();
    }

    @Test
    void updateRequiresEmailOrPhone() {
        var existing = User.builder().id(1L).name("Ana").email("ana@mysky.ge").role(Role.WORKER).active(true).build();
        when(users.findByIdAndRole(1L, Role.WORKER)).thenReturn(Optional.of(existing));

        var req = new WorkerUpdateRequest("Ana", null, null, null);

        assertThatThrownBy(() -> service.update(1L, req)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateAllowsKeepingOwnEmail() {
        var existing = User.builder().id(1L).name("Ana").email("ana@mysky.ge").role(Role.WORKER).active(true).build();
        when(users.findByIdAndRole(1L, Role.WORKER)).thenReturn(Optional.of(existing));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new WorkerUpdateRequest("Ana Updated", "ana@mysky.ge", null, null);
        var updated = service.update(1L, req);

        assertThat(updated.getName()).isEqualTo("Ana Updated");
        assertThat(updated.getEmail()).isEqualTo("ana@mysky.ge");
    }

    @Test
    void updateRejectsEmailTakenByAnotherWorker() {
        var existing = User.builder().id(1L).name("Ana").email("ana@mysky.ge").role(Role.WORKER).active(true).build();
        when(users.findByIdAndRole(1L, Role.WORKER)).thenReturn(Optional.of(existing));
        when(users.existsByEmailIgnoreCase("other@mysky.ge")).thenReturn(true);

        var req = new WorkerUpdateRequest("Ana", "other@mysky.ge", null, null);

        assertThatThrownBy(() -> service.update(1L, req)).isInstanceOf(ConflictException.class);
    }

    @Test
    void updateOnlyRehashesPasswordWhenProvided() {
        var existing = User.builder().id(1L).name("Ana").email("ana@mysky.ge")
                .passwordHash("old-hash").role(Role.WORKER).active(true).build();
        when(users.findByIdAndRole(1L, Role.WORKER)).thenReturn(Optional.of(existing));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new WorkerUpdateRequest("Ana", "ana@mysky.ge", null, null);
        var updated = service.update(1L, req);

        assertThat(updated.getPasswordHash()).isEqualTo("old-hash");
    }

    @Test
    void deleteMarksInactiveInsteadOfRemoving() {
        var existing = User.builder().id(1L).name("Ana").role(Role.WORKER).active(true).build();
        when(users.findByIdAndRole(1L, Role.WORKER)).thenReturn(Optional.of(existing));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.delete(1L);

        assertThat(existing.isActive()).isFalse();
        verify(users).save(existing);
    }
}
