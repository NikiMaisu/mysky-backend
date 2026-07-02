package ge.mysky.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_materials")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class OrderMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "material_id")
    private Long materialId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "unit_price_m2", nullable = false)
    private BigDecimal unitPricePerM2;

    @Column(name = "unit_time_min", nullable = false)
    private BigDecimal unitTimeMinutes;

    @Column(name = "square_meters", nullable = false)
    private BigDecimal squareMeters;
}
