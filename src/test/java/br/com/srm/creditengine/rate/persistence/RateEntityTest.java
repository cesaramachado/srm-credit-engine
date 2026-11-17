package br.com.srm.creditengine.rate.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RateEntityTest {

    @Test
    void retainsBaseRateAndItsValidityWindow() {
        Instant validFrom = Instant.parse("2026-11-17T12:00:00Z");
        Instant validTo = validFrom.plusSeconds(3600);
        BaseRateEntity entity = new BaseRateEntity(new BigDecimal("0.01"), validFrom, validTo);

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getMonthlyRate()).isEqualByComparingTo("0.01");
        assertThat(entity.getValidFrom()).isEqualTo(validFrom);
        assertThat(entity.getValidTo()).isEqualTo(validTo);
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void retainsExchangeRatePairAndItsValidityWindow() {
        Instant validFrom = Instant.parse("2026-11-17T12:00:00Z");
        ExchangeRateEntity entity = new ExchangeRateEntity(
                CurrencyCode.USD, CurrencyCode.BRL, new BigDecimal("5.4321"), validFrom, null);

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getSourceCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(entity.getTargetCurrency()).isEqualTo(CurrencyCode.BRL);
        assertThat(entity.getRate()).isEqualByComparingTo("5.4321");
        assertThat(entity.getValidFrom()).isEqualTo(validFrom);
        assertThat(entity.getValidTo()).isNull();
        assertThat(entity.getCreatedAt()).isNotNull();
    }
}
