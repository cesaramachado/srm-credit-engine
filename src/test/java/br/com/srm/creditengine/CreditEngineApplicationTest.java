package br.com.srm.creditengine;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.srm.creditengine.pricing.domain.PricingEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CreditEngineApplicationTest {

    @Autowired
    private PricingEngine pricingEngine;

    @Test
    void startsApplicationAndWiresPricingStrategies() {
        assertThat(pricingEngine).isNotNull();
    }
}
