package ge.mysky.backend.repository;

import ge.mysky.backend.domain.Fixture;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    List<Fixture> findAllByActiveTrueOrderByNameAsc();
}
