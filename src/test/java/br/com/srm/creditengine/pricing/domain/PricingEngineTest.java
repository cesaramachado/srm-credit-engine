package br.com.srm.creditengine.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PricingEngineTest {

    private static final BigDecimal GOLDEN_BASE_RATE = new BigDecimal("0.01");

    private PricingEngine pricingEngine;

    @BeforeEach
    void setUp() {
        pricingEngine = new PricingEngine(List.of(
                new DuplicataPricingStrategy(),
                new ChequePricingStrategy()));
    }

    @Test
    void calculatesGoldenCaseOneForDuplicataInBrl() {
        PricingResult result = pricingEngine.calculate(new PricingRequest(
                new BigDecimal("100000.00"),
                3,
                GOLDEN_BASE_RATE,
                ReceivableType.DUPLICATA_MERCANTIL,
                CurrencyCode.BRL,
                null));

        assertThat(result.presentValueBrl()).isEqualByComparingTo("92859.94");
        assertThat(result.discountBrl()).isEqualByComparingTo("7140.06");
        assertThat(result.settlementAmount()).isEqualByComparingTo("92859.94");
        assertThat(result.monthlySpread()).isEqualByComparingTo("0.015");
        assertThat(result.exchangeRateUsed()).isNull();
    }

    @Test
    void calculatesGoldenCaseTwoForChequeInBrl() {
        PricingResult result = pricingEngine.calculate(new PricingRequest(
                new BigDecimal("25000.00"),
                2,
                GOLDEN_BASE_RATE,
                ReceivableType.CHEQUE_PRE_DATADO,
                CurrencyCode.BRL,
                null));

        assertThat(result.presentValueBrl()).isEqualByComparingTo("23337.77");
        assertThat(result.discountBrl()).isEqualByComparingTo("1662.23");
        assertThat(result.settlementAmount()).isEqualByComparingTo("23337.77");
        assertThat(result.monthlySpread()).isEqualByComparingTo("0.025");
    }

    @Test
    void calculatesGoldenCaseThreeByRoundingBrlBeforeUsdConversion() {
        PricingResult result = pricingEngine.calculate(new PricingRequest(
                new BigDecimal("100000.00"),
                3,
                GOLDEN_BASE_RATE,
                ReceivableType.DUPLICATA_MERCANTIL,
                CurrencyCode.USD,
                new BigDecimal("5.4321")));

        assertThat(result.presentValueBrl()).isEqualByComparingTo("92859.94");
        assertThat(result.discountBrl()).isEqualByComparingTo("7140.06");
        assertThat(result.settlementAmount()).isEqualByComparingTo("17094.67");
        assertThat(result.exchangeRateUsed()).isEqualByComparingTo("5.4321");
    }

    @Test
    void requiresAnExchangeRateForUsdSettlement() {
        PricingRequest request = new PricingRequest(
                new BigDecimal("100000.00"),
                3,
                GOLDEN_BASE_RATE,
                ReceivableType.DUPLICATA_MERCANTIL,
                CurrencyCode.USD,
                null);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> pricingEngine.calculate(request))
                .withMessage("brlPerUsd must not be null");
    }

    @Test
    void rejectsFractionalOrNonPositiveTermRepresentation() {
        PricingRequest request = new PricingRequest(
                new BigDecimal("100000.00"),
                0,
                GOLDEN_BASE_RATE,
                ReceivableType.DUPLICATA_MERCANTIL,
                CurrencyCode.BRL,
                null);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> pricingEngine.calculate(request))
                .withMessage("termMonths must be greater than zero");
    }
}
