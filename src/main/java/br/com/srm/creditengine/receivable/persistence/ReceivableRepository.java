package br.com.srm.creditengine.receivable.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivableRepository extends JpaRepository<ReceivableEntity, UUID> {
}
