package br.com.srm.creditengine.pricing.application;

import br.com.srm.creditengine.pricing.domain.PricingEngine;
import br.com.srm.creditengine.pricing.domain.PricingRequest;
import br.com.srm.creditengine.pricing.domain.PricingResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class PricingSimulationApplicationService {

    private final PricingEngine pricingEngine;
    private final MeterRegistry meterRegistry;

    public PricingSimulationApplicationService(PricingEngine pricingEngine, MeterRegistry meterRegistry) {
        this.pricingEngine = pricingEngine;
        this.meterRegistry = meterRegistry;
    }

    public PricingResult simulate(PricingRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        try {
            return pricingEngine.calculate(request);
        } catch (RuntimeException exception) {
            outcome = "failure";
            throw exception;
        } finally {
            sample.stop(Timer.builder("srm.pricing.calculations")
                    .description("Pricing simulation duration")
                    .tag("outcome", outcome)
                    .register(meterRegistry));
        }
    }
}
