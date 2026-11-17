package br.com.srm.creditengine.receivable.application;

import br.com.srm.creditengine.common.exception.ReceivableNotFoundException;
import br.com.srm.creditengine.pricing.domain.ReceivableType;
import br.com.srm.creditengine.receivable.persistence.ReceivableEntity;
import br.com.srm.creditengine.receivable.persistence.ReceivableRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceivableApplicationService {

    private final ReceivableRepository receivableRepository;

    public ReceivableApplicationService(ReceivableRepository receivableRepository) {
        this.receivableRepository = receivableRepository;
    }

    @Transactional
    public ReceivableEntity create(String assignor, BigDecimal faceValueBrl, LocalDate dueDate,
            ReceivableType receivableType) {
        return receivableRepository.save(new ReceivableEntity(assignor, faceValueBrl, dueDate, receivableType));
    }

    @Transactional(readOnly = true)
    public ReceivableEntity get(UUID id) {
        return receivableRepository.findById(id)
                .orElseThrow(() -> new ReceivableNotFoundException(id));
    }
}
