package br.com.srm.creditengine.pricing.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class PricingEngine {

    private static final MathContext WORKING_PRECISION = MathContext.DECIMAL128;
    private static final int MONEY_SCALE = 2;

    private final Map<ReceivableType, PricingStrategy> strategies;

    public PricingEngine(List<PricingStrategy> pricingStrategies) {
        Map<ReceivableType, PricingStrategy> indexedStrategies = new EnumMap<>(ReceivableType.class);
        for (PricingStrategy strategy : pricingStrategies) {
            PricingStrategy previous = indexedStrategies.put(strategy.receivableType(), strategy);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "More than one pricing strategy registered for " + strategy.receivableType());
            }
        }
        this.strategies = Map.copyOf(indexedStrategies);
    }

    public PricingResult calculate(PricingRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        requirePositive(request.faceValueBrl(), "faceValueBrl");
        requireNonNull(request.baseMonthlyRate(), "baseMonthlyRate");
        requireNonNull(request.receivableType(), "receivableType");
        requireNonNull(request.paymentCurrency(), "paymentCurrency");
        if (request.termMonths() <= 0) {
            throw new IllegalArgumentException("termMonths must be greater than zero");
        }

        PricingStrategy strategy = strategies.get(request.receivableType());
        if (strategy == null) {
            throw new IllegalArgumentException("No pricing strategy for " + request.receivableType());
        }

        BigDecimal spread = strategy.monthlySpread();
        BigDecimal monthlyFactor = BigDecimal.ONE
                .add(request.baseMonthlyRate())
                .add(spread);
        if (monthlyFactor.signum() <= 0) {
            throw new IllegalArgumentException("The monthly factor must be greater than zero");
        }

        BigDecimal divisor = monthlyFactor.pow(request.termMonths());
        BigDecimal presentValueBrl = request.faceValueBrl()
                .divide(divisor, WORKING_PRECISION)
                .setScale(MONEY_SCALE, RoundingMode.HALF_EVEN);
        BigDecimal discountBrl = request.faceValueBrl()
                .subtract(presentValueBrl)
                .setScale(MONEY_SCALE, RoundingMode.HALF_EVEN);

        BigDecimal settlementAmount;
        BigDecimal exchangeRateUsed = null;
        if (request.paymentCurrency() == CurrencyCode.BRL) {
            settlementAmount = presentValueBrl;
        } else {
            requirePositive(request.brlPerUsd(), "brlPerUsd");
            exchangeRateUsed = request.brlPerUsd();
            settlementAmount = presentValueBrl
                    .divide(exchangeRateUsed, WORKING_PRECISION)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_EVEN);
        }

        return new PricingResult(
                presentValueBrl,
                discountBrl,
                settlementAmount,
                request.paymentCurrency(),
                request.baseMonthlyRate(),
                spread,
                request.termMonths(),
                exchangeRateUsed);
    }

    private static void requirePositive(BigDecimal value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }
}
