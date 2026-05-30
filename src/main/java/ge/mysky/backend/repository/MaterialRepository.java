package ge.mysky.backend.repository;

import ge.mysky.backend.domain.Material;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findAllByActiveTrueOrderByNameAsc();
}
