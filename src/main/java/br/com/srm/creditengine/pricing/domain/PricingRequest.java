package br.com.srm.creditengine.pricing.domain;

import java.math.BigDecimal;

/**
 * Input for pricing a receivable whose face value is denominated in BRL.
 * Rates are fractions: for example, 1% is represented by 0.01.
 * The exchange rate, when present, is BRL per USD.
 */
public record PricingRequest(
        BigDecimal faceValueBrl,
        int termMonths,
        BigDecimal baseMonthlyRate,
        ReceivableType receivableType,
        CurrencyCode paymentCurrency,
        BigDecimal brlPerUsd) {
}
