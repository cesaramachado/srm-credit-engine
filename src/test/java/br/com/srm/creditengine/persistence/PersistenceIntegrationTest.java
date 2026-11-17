package br.com.srm.creditengine.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import br.com.srm.creditengine.pricing.domain.ReceivableType;
import br.com.srm.creditengine.rate.persistence.BaseRateEntity;
import br.com.srm.creditengine.rate.persistence.BaseRateRepository;
import br.com.srm.creditengine.rate.persistence.ExchangeRateEntity;
import br.com.srm.creditengine.rate.persistence.ExchangeRateRepository;
import br.com.srm.creditengine.receivable.domain.ReceivableStatus;
import br.com.srm.creditengine.receivable.persistence.ReceivableEntity;
import br.com.srm.creditengine.receivable.persistence.ReceivableRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mssqlserver.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("test")
class PersistenceIntegrationTest {

    @Container
    private static final MSSQLServerContainer SQL_SERVER = new MSSQLServerContainer(
            DockerImageName.parse("mcr.microsoft.com/mssql/server:2022-CU20-ubuntu-22.04"))
            .acceptLicense()
            .withPassword("TestOnly_StrongPassword2026!");

    @Autowired
    private ReceivableRepository receivableRepository;

    @Autowired
    private BaseRateRepository baseRateRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SQL_SERVER::getJdbcUrl);
        registry.add("spring.datasource.username", SQL_SERVER::getUsername);
        registry.add("spring.datasource.password", SQL_SERVER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    @Test
    void appliesFlywayMigrationsAndPersistsReceivablesAndEffectiveRates() {
        Instant now = Instant.now();
        ReceivableEntity receivable = receivableRepository.saveAndFlush(new ReceivableEntity(
                "cedente-teste",
                new BigDecimal("100000.00"),
                LocalDate.of(2027, 2, 15),
                ReceivableType.DUPLICATA_MERCANTIL));
        BaseRateEntity baseRate = baseRateRepository.saveAndFlush(new BaseRateEntity(
                new BigDecimal("0.01"), now.minusSeconds(60), null));
        ExchangeRateEntity exchangeRate = exchangeRateRepository.saveAndFlush(new ExchangeRateEntity(
                CurrencyCode.USD,
                CurrencyCode.BRL,
                new BigDecimal("5.4321"),
                now.minusSeconds(60),
                null));

        assertThat(receivableRepository.findById(receivable.getId()))
                .get()
                .extracting(ReceivableEntity::getStatus)
                .isEqualTo(ReceivableStatus.OPEN);
        assertThat(baseRateRepository.findEffectiveAt(now))
                .extracting(BaseRateEntity::getId)
                .contains(baseRate.getId());
        assertThat(exchangeRateRepository.findEffectiveAt(CurrencyCode.USD, CurrencyCode.BRL, now))
                .extracting(ExchangeRateEntity::getId)
                .contains(exchangeRate.getId());
    }
}
