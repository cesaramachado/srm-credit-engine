package br.com.srm.creditengine.rate.persistence;

import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, UUID> {

    Page<ExchangeRateEntity> findBySourceCurrencyAndTargetCurrency(
            CurrencyCode sourceCurrency,
            CurrencyCode targetCurrency,
            Pageable pageable);

    @Query("""
            select rate from ExchangeRateEntity rate
            where rate.sourceCurrency = :sourceCurrency
              and rate.targetCurrency = :targetCurrency
              and rate.validFrom <= :instant
              and (rate.validTo is null or rate.validTo > :instant)
            order by rate.validFrom desc
            """)
    List<ExchangeRateEntity> findEffectiveAt(
            @Param("sourceCurrency") CurrencyCode sourceCurrency,
            @Param("targetCurrency") CurrencyCode targetCurrency,
            @Param("instant") Instant instant);
}
