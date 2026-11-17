package br.com.srm.creditengine.receivable.api;

import br.com.srm.creditengine.pricing.domain.ReceivableType;
import br.com.srm.creditengine.receivable.domain.ReceivableStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReceivableResponse(
        UUID id,
        String assignor,
        BigDecimal faceValueBrl,
        LocalDate dueDate,
        ReceivableType receivableType,
        ReceivableStatus status,
        long version,
        Instant createdAt) {
}
