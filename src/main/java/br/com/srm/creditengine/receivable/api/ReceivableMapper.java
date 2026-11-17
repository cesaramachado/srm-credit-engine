package br.com.srm.creditengine.receivable.api;

import br.com.srm.creditengine.receivable.persistence.ReceivableEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReceivableMapper {

    ReceivableResponse toResponse(ReceivableEntity entity);
}
