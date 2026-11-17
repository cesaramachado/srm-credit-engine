package br.com.srm.creditengine.rate.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import br.com.srm.creditengine.rate.persistence.ExchangeRateEntity;
import br.com.srm.creditengine.rate.persistence.ExchangeRateRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExchangeRateApplicationServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private ExchangeRateApplicationService service;

    @Test
    void recordsRatePairAndValidityWithoutChangingTheRate() {
        when(exchangeRateRepository.save(any(ExchangeRateEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Instant validFrom = Instant.parse("2026-11-17T12:00:00Z");

        ExchangeRateEntity result = service.create(
                CurrencyCode.USD,
                CurrencyCode.BRL,
                new BigDecimal("5.4321"),
                validFrom,
                null);

        assertThat(result.getSourceCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(result.getTargetCurrency()).isEqualTo(CurrencyCode.BRL);
        assertThat(result.getRate()).isEqualByComparingTo("5.4321");
        assertThat(result.getValidFrom()).isEqualTo(validFrom);
        verify(exchangeRateRepository).save(any(ExchangeRateEntity.class));
    }

    @Test
    void listsAllRatesWhenNoPairFilterIsProvided() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(exchangeRateRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        assertThat(service.history(null, null, pageable).getContent()).isEmpty();
        verify(exchangeRateRepository).findAll(pageable);
    }

    @Test
    void listsRatesForACompleteCurrencyPair() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(exchangeRateRepository.findBySourceCurrencyAndTargetCurrency(
                CurrencyCode.USD, CurrencyCode.BRL, pageable)).thenReturn(new PageImpl<>(List.of()));

        assertThat(service.history(CurrencyCode.USD, CurrencyCode.BRL, pageable).getContent()).isEmpty();
        verify(exchangeRateRepository).findBySourceCurrencyAndTargetCurrency(
                CurrencyCode.USD, CurrencyCode.BRL, pageable);
    }

    @Test
    void requiresBothCurrenciesForAPairFilter() {
        assertThatThrownBy(() -> service.history(CurrencyCode.USD, null, PageRequest.of(0, 20)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be provided together");
    }
}
