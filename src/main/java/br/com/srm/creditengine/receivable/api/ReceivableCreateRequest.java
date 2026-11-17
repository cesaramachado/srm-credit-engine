package br.com.srm.creditengine.receivable.api;

import br.com.srm.creditengine.pricing.domain.ReceivableType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceivableCreateRequest(
        @NotBlank @Size(max = 200) String assignor,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 36, fraction = 2)
        BigDecimal faceValueBrl,
        @NotNull LocalDate dueDate,
        @NotNull ReceivableType receivableType) {
}
