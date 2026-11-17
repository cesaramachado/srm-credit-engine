package br.com.srm.creditengine.rate.api;

import br.com.srm.creditengine.rate.persistence.ExchangeRateEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExchangeRateMapper {

    ExchangeRateResponse toResponse(ExchangeRateEntity entity);
}
