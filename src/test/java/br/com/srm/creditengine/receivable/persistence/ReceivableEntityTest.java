package br.com.srm.creditengine.receivable.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.srm.creditengine.pricing.domain.ReceivableType;
import br.com.srm.creditengine.receivable.domain.ReceivableStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReceivableEntityTest {

    @Test
    void initializesAnOpenReceivableWithAnIdentifierAndCreationTime() {
        LocalDate dueDate = LocalDate.of(2027, 2, 15);
        ReceivableEntity entity = new ReceivableEntity(
                "cedente-teste", new BigDecimal("100.00"), dueDate, ReceivableType.DUPLICATA_MERCANTIL);

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getAssignor()).isEqualTo("cedente-teste");
        assertThat(entity.getFaceValueBrl()).isEqualByComparingTo("100.00");
        assertThat(entity.getDueDate()).isEqualTo(dueDate);
        assertThat(entity.getReceivableType()).isEqualTo(ReceivableType.DUPLICATA_MERCANTIL);
        assertThat(entity.getStatus()).isEqualTo(ReceivableStatus.OPEN);
        assertThat(entity.getVersion()).isZero();
        assertThat(entity.getCreatedAt()).isNotNull();
    }
}
