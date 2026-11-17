package br.com.srm.creditengine.rate.api;

import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import br.com.srm.creditengine.rate.application.ExchangeRateApplicationService;
import br.com.srm.creditengine.rate.persistence.ExchangeRateEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exchange-rates")
@Tag(name = "Exchange rates")
@SecurityRequirement(name = "keycloak-oauth2")
public class ExchangeRateController {

    private final ExchangeRateApplicationService exchangeRateService;
    private final ExchangeRateMapper exchangeRateMapper;

    public ExchangeRateController(ExchangeRateApplicationService exchangeRateService,
            ExchangeRateMapper exchangeRateMapper) {
        this.exchangeRateService = exchangeRateService;
        this.exchangeRateMapper = exchangeRateMapper;
    }

    @PostMapping
    @Operation(summary = "Register a locally configured exchange rate")
    public ResponseEntity<ExchangeRateResponse> create(@Valid @RequestBody ExchangeRateCreateRequest request) {
        ExchangeRateEntity created = exchangeRateService.create(
                request.sourceCurrency(),
                request.targetCurrency(),
                request.rate(),
                request.validFrom(),
                request.validTo());
        return ResponseEntity.created(URI.create("/api/v1/exchange-rates/" + created.getId()))
                .body(exchangeRateMapper.toResponse(created));
    }

    @GetMapping
    @Operation(summary = "List exchange rate history")
    public PagedModel<ExchangeRateResponse> history(
            @RequestParam(required = false) CurrencyCode sourceCurrency,
            @RequestParam(required = false) CurrencyCode targetCurrency,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(exchangeRateService.history(sourceCurrency, targetCurrency, pageable)
                .map(exchangeRateMapper::toResponse));
    }
}
