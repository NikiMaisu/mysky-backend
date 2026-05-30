package ge.mysky.backend.repository;

import ge.mysky.backend.domain.GraniteConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraniteConfigRepository extends JpaRepository<GraniteConfig, Short> {
}
