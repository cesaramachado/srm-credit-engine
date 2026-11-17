package br.com.srm.creditengine.receivable.persistence;

import br.com.srm.creditengine.pricing.domain.ReceivableType;
import br.com.srm.creditengine.receivable.domain.ReceivableStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "receivables")
public class ReceivableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "assignor", nullable = false, length = 200)
    private String assignor;

    @Column(name = "face_value_brl", nullable = false, precision = 38, scale = 2)
    private BigDecimal faceValueBrl;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "receivable_type", nullable = false, length = 32)
    private ReceivableType receivableType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReceivableStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ReceivableEntity() {
    }

    public ReceivableEntity(String assignor, BigDecimal faceValueBrl, LocalDate dueDate,
            ReceivableType receivableType) {
        this.id = UUID.randomUUID();
        this.assignor = assignor;
        this.faceValueBrl = faceValueBrl;
        this.dueDate = dueDate;
        this.receivableType = receivableType;
        this.status = ReceivableStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getAssignor() {
        return assignor;
    }

    public BigDecimal getFaceValueBrl() {
        return faceValueBrl;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public ReceivableType getReceivableType() {
        return receivableType;
    }

    public ReceivableStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
