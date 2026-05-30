package ge.mysky.backend.repository;

import ge.mysky.backend.domain.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = "members")
    List<Team> findAllByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = "members")
    Optional<Team> findWithMembersById(Long id);
}
