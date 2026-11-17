package br.com.srm.creditengine.rate.api;

import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateCreateRequest(
        @NotNull CurrencyCode sourceCurrency,
        @NotNull CurrencyCode targetCurrency,
        @NotNull @Positive BigDecimal rate,
        @NotNull Instant validFrom,
        Instant validTo) {

    @AssertTrue(message = "sourceCurrency and targetCurrency must differ")
    public boolean isCurrencyPairValid() {
        return sourceCurrency == null || targetCurrency == null || sourceCurrency != targetCurrency;
    }

    @AssertTrue(message = "validTo must be later than validFrom")
    public boolean isValidityPeriodValid() {
        return validFrom == null || validTo == null || validTo.isAfter(validFrom);
    }
}
