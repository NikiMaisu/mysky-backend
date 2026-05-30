package ge.mysky.backend.repository;

import ge.mysky.backend.domain.AddonCategory;
import ge.mysky.backend.domain.AddonInstance;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddonInstanceRepository extends JpaRepository<AddonInstance, Long> {

    List<AddonInstance> findAllByActiveTrueOrderByNameAsc();

    List<AddonInstance> findAllByActiveTrueAndCategoryOrderByNameAsc(AddonCategory category);
}
