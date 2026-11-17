package br.com.srm.creditengine.pricing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import br.com.srm.creditengine.pricing.domain.ChequePricingStrategy;
import br.com.srm.creditengine.pricing.domain.DuplicataPricingStrategy;
import br.com.srm.creditengine.pricing.domain.PricingEngine;
import br.com.srm.creditengine.pricing.domain.PricingRequest;
import br.com.srm.creditengine.pricing.domain.ReceivableType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PricingSimulationApplicationServiceTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final PricingSimulationApplicationService service = new PricingSimulationApplicationService(
            new PricingEngine(java.util.List.of(new DuplicataPricingStrategy(), new ChequePricingStrategy())),
            meterRegistry);

    @Test
    void recordsDurationForSuccessfulSimulation() {
        var result = service.simulate(new PricingRequest(
                new BigDecimal("100000.00"), 3, new BigDecimal("0.01"),
                ReceivableType.DUPLICATA_MERCANTIL, CurrencyCode.BRL, null));

        assertThat(result.presentValueBrl()).isEqualByComparingTo("92859.94");
        assertThat(meterRegistry.get("srm.pricing.calculations")
                .tag("outcome", "success")
                .timer()
                .count()).isEqualTo(1);
    }

    @Test
    void recordsDurationForRejectedSimulation() {
        assertThatThrownBy(() -> service.simulate(new PricingRequest(
                new BigDecimal("100.00"), 0, new BigDecimal("0.01"),
                ReceivableType.DUPLICATA_MERCANTIL, CurrencyCode.BRL, null)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(meterRegistry.get("srm.pricing.calculations")
                .tag("outcome", "failure")
                .timer()
                .count()).isEqualTo(1);
    }
}
