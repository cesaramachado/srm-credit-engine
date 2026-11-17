package br.com.srm.creditengine.pricing.domain;

import java.math.BigDecimal;

public record PricingResult(
        BigDecimal presentValueBrl,
        BigDecimal discountBrl,
        BigDecimal settlementAmount,
        CurrencyCode settlementCurrency,
        BigDecimal baseMonthlyRate,
        BigDecimal monthlySpread,
        int termMonths,
        BigDecimal exchangeRateUsed) {
}
