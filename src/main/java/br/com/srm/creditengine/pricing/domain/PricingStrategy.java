package br.com.srm.creditengine.pricing.domain;

import java.math.BigDecimal;

public interface PricingStrategy {

    ReceivableType receivableType();

    BigDecimal monthlySpread();
}
