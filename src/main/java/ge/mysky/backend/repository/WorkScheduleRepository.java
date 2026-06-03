package ge.mysky.backend.repository;

import ge.mysky.backend.domain.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Short> {
}
