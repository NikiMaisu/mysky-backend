package ge.mysky.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "granite_config")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GraniteConfig {

    public static final short SINGLETON_ID = 1;

    @Id
    private Short id;

    @Column(name = "price_per_meter", nullable = false)
    private BigDecimal pricePerMeter;

    @Column(name = "time_per_meter_min", nullable = false)
    private BigDecimal timePerMeterMinutes;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
