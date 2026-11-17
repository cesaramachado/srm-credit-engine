package br.com.srm.creditengine.rate.application;

import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import br.com.srm.creditengine.rate.persistence.ExchangeRateEntity;
import br.com.srm.creditengine.rate.persistence.ExchangeRateRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExchangeRateApplicationService {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateApplicationService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Transactional
    public ExchangeRateEntity create(CurrencyCode sourceCurrency, CurrencyCode targetCurrency,
            BigDecimal rate, Instant validFrom, Instant validTo) {
        return exchangeRateRepository.save(new ExchangeRateEntity(
                sourceCurrency, targetCurrency, rate, validFrom, validTo));
    }

    @Transactional(readOnly = true)
    public Page<ExchangeRateEntity> history(CurrencyCode sourceCurrency, CurrencyCode targetCurrency,
            Pageable pageable) {
        if (sourceCurrency == null && targetCurrency == null) {
            return exchangeRateRepository.findAll(pageable);
        }
        if (sourceCurrency == null || targetCurrency == null) {
            throw new IllegalArgumentException("sourceCurrency and targetCurrency must be provided together");
        }
        return exchangeRateRepository.findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency, pageable);
    }

}
