package br.com.srm.creditengine.pricing.domain;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class DuplicataPricingStrategy implements PricingStrategy {

    private static final BigDecimal MONTHLY_SPREAD = new BigDecimal("0.015");

    @Override
    public ReceivableType receivableType() {
        return ReceivableType.DUPLICATA_MERCANTIL;
    }

    @Override
    public BigDecimal monthlySpread() {
        return MONTHLY_SPREAD;
    }
}
