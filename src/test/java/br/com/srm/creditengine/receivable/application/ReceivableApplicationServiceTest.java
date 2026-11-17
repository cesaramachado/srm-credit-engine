package br.com.srm.creditengine.receivable.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.srm.creditengine.pricing.domain.ReceivableType;
import br.com.srm.creditengine.receivable.domain.ReceivableStatus;
import br.com.srm.creditengine.receivable.persistence.ReceivableEntity;
import br.com.srm.creditengine.receivable.persistence.ReceivableRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReceivableApplicationServiceTest {

    @Mock
    private ReceivableRepository receivableRepository;

    @InjectMocks
    private ReceivableApplicationService service;

    @Test
    void createsReceivableInOpenState() {
        when(receivableRepository.save(any(ReceivableEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReceivableEntity result = service.create(
                "cedente-teste",
                new BigDecimal("100000.00"),
                LocalDate.of(2027, 2, 15),
                ReceivableType.DUPLICATA_MERCANTIL);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ReceivableStatus.OPEN);
        assertThat(result.getFaceValueBrl()).isEqualByComparingTo("100000.00");
        verify(receivableRepository).save(any(ReceivableEntity.class));
    }

    @Test
    void returnsReceivableWhenItExists() {
        UUID id = UUID.randomUUID();
        ReceivableEntity receivable = new ReceivableEntity(
                "cedente-teste", new BigDecimal("100.00"), LocalDate.of(2027, 2, 15),
                ReceivableType.CHEQUE_PRE_DATADO);
        when(receivableRepository.findById(id)).thenReturn(Optional.of(receivable));

        assertThat(service.get(id)).isSameAs(receivable);
    }

    @Test
    void throwsSpecificExceptionWhenReceivableDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(receivableRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(br.com.srm.creditengine.common.exception.ReceivableNotFoundException.class);
    }
}
