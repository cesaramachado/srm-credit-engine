package br.com.srm.creditengine.rate.api;

import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExchangeRateResponse(
        UUID id,
        CurrencyCode sourceCurrency,
        CurrencyCode targetCurrency,
        BigDecimal rate,
        Instant validFrom,
        Instant validTo,
        Instant createdAt) {
}
