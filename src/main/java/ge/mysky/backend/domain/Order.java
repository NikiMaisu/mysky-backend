package ge.mysky.backend.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Generated(event = EventType.INSERT)
    @Column(name = "order_number", insertable = false, updatable = false)
    private Long orderNumber;

    @Column(name = "client_name", nullable = false, length = 160)
    private String clientName;

    @Column(name = "client_phone", length = 40)
    private String clientPhone;

    @Column(length = 400)
    private String address;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "finish_at", nullable = false)
    private OffsetDateTime finishAt;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "team_name", length = 120)
    private String teamName;

    @Column(name = "material_id")
    private Long materialId;

    @Column(name = "material_name", nullable = false, length = 120)
    private String materialName;

    @Column(name = "material_price_per_m2", nullable = false)
    private BigDecimal materialPricePerM2;

    @Column(name = "material_time_per_m2_min", nullable = false)
    private BigDecimal materialTimePerM2Minutes;

    @Column(name = "square_meters", nullable = false)
    private BigDecimal squareMeters;

    @Column(name = "granite_enabled", nullable = false)
    private boolean graniteEnabled;

    @Column(name = "perimeter")
    private BigDecimal perimeter;

    @Column(name = "granite_price_per_meter")
    private BigDecimal granitePricePerMeter;

    @Column(name = "granite_time_per_meter_min")
    private BigDecimal graniteTimePerMeterMinutes;

    @Column(name = "flat_added_min", nullable = false)
    private int flatAddedMinutes;

    @Column(name = "total_min", nullable = false)
    private int totalMinutes;

    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status;

    @Column(columnDefinition = "text")
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderFixture> fixtures = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderAddon> addons = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public void addFixture(OrderFixture f) {
        f.setOrder(this);
        fixtures.add(f);
    }

    public void addAddon(OrderAddon a) {
        a.setOrder(this);
        addons.add(a);
    }

    @PrePersist
    void onCreate() {
        var now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
