package br.com.srm.creditengine.rate.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "base_rates")
public class BaseRateEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "monthly_rate", nullable = false, precision = 38, scale = 18)
    private BigDecimal monthlyRate;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BaseRateEntity() {
    }

    public BaseRateEntity(BigDecimal monthlyRate, Instant validFrom, Instant validTo) {
        this.id = UUID.randomUUID();
        this.monthlyRate = monthlyRate;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getMonthlyRate() {
        return monthlyRate;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
