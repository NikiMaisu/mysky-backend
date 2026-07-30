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

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false)
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

    @Column(name = "finish_overridden", nullable = false)
    private boolean finishOverridden;

    @Column(name = "total_min", nullable = false)
    private int totalMinutes;

    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost;

    @Column(name = "cost_overridden", nullable = false)
    private boolean costOverridden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status;

    @Column(columnDefinition = "text")
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderMaterial> materials = new ArrayList<>();

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

    public void addMaterial(OrderMaterial m) {
        m.setOrder(this);
        materials.add(m);
    }

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
