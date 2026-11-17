package br.com.srm.creditengine.rate.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BaseRateRepository extends JpaRepository<BaseRateEntity, UUID> {

    @Query("""
            select rate from BaseRateEntity rate
            where rate.validFrom <= :instant
              and (rate.validTo is null or rate.validTo > :instant)
            order by rate.validFrom desc
            """)
    List<BaseRateEntity> findEffectiveAt(@Param("instant") Instant instant);
}
