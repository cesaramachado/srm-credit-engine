package br.com.srm.creditengine.pricing.api;

import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import br.com.srm.creditengine.pricing.domain.ReceivableType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PricingSimulationRequest(
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal faceValueBrl,
        @NotNull @Positive Integer termMonths,
        @NotNull BigDecimal baseMonthlyRate,
        @NotNull ReceivableType receivableType,
        @NotNull CurrencyCode paymentCurrency,
        @DecimalMin(value = "0", inclusive = false) BigDecimal brlPerUsd) {
}
