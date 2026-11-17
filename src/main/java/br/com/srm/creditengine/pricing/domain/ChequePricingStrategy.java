package br.com.srm.creditengine.pricing.domain;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ChequePricingStrategy implements PricingStrategy {

    private static final BigDecimal MONTHLY_SPREAD = new BigDecimal("0.025");

    @Override
    public ReceivableType receivableType() {
        return ReceivableType.CHEQUE_PRE_DATADO;
    }

    @Override
    public BigDecimal monthlySpread() {
        return MONTHLY_SPREAD;
    }
}
