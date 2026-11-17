package br.com.srm.creditengine.rate.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.srm.creditengine.common.exception.ApiExceptionHandler;
import br.com.srm.creditengine.config.ApiSecurityConfig;
import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import br.com.srm.creditengine.rate.application.ExchangeRateApplicationService;
import br.com.srm.creditengine.rate.persistence.ExchangeRateEntity;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExchangeRateController.class)
@Import({ApiExceptionHandler.class, ApiSecurityConfig.class})
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeRateApplicationService exchangeRateService;

    @MockitoBean
    private ExchangeRateMapper exchangeRateMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void requiresAuthenticationForBusinessEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/exchange-rates")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void registersAnExchangeRateWithItsValidity() throws Exception {
        Instant validFrom = Instant.parse("2026-11-17T12:00:00Z");
        ExchangeRateEntity entity = new ExchangeRateEntity(
                CurrencyCode.USD,
                CurrencyCode.BRL,
                new BigDecimal("5.4321"),
                validFrom,
                null);
        ExchangeRateResponse response = new ExchangeRateResponse(
                entity.getId(),
                CurrencyCode.USD,
                CurrencyCode.BRL,
                new BigDecimal("5.4321"),
                validFrom,
                null,
                entity.getCreatedAt());
        when(exchangeRateService.create(
                eq(CurrencyCode.USD),
                eq(CurrencyCode.BRL),
                eq(new BigDecimal("5.4321")),
                eq(validFrom),
                eq(null)))
                .thenReturn(entity);
        when(exchangeRateMapper.toResponse(any(ExchangeRateEntity.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/exchange-rates")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "sourceCurrency": "USD",
                                  "targetCurrency": "BRL",
                                  "rate": 5.4321,
                                  "validFrom": "2026-11-17T12:00:00Z",
                                  "validTo": null
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/exchange-rates/" + entity.getId()))
                .andExpect(jsonPath("$.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrency").value("BRL"))
                .andExpect(jsonPath("$.rate").value(5.4321));
    }

    @Test
    @WithMockUser
    void rejectsAnInvalidCurrencyPair() throws Exception {
        mockMvc.perform(post("/api/v1/exchange-rates")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "sourceCurrency": "USD",
                                  "targetCurrency": "USD",
                                  "rate": 5.4321,
                                  "validFrom": "2026-11-17T12:00:00Z",
                                  "validTo": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser
    void listsAFilteredAndPaginatedRateHistory() throws Exception {
        PageRequest pageable = PageRequest.of(0, 20);
        Instant validFrom = Instant.parse("2026-11-17T12:00:00Z");
        ExchangeRateEntity entity = new ExchangeRateEntity(
                CurrencyCode.USD, CurrencyCode.BRL, new BigDecimal("5.4321"), validFrom, null);
        ExchangeRateResponse response = new ExchangeRateResponse(
                entity.getId(), CurrencyCode.USD, CurrencyCode.BRL, entity.getRate(), validFrom,
                null, entity.getCreatedAt());
        when(exchangeRateService.history(null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(exchangeRateMapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(get("/api/v1/exchange-rates?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rate").value(5.4321))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void rejectsAnIncompleteCurrencyPairFilter() throws Exception {
        when(exchangeRateService.history(eq(CurrencyCode.USD), eq(null), any()))
                .thenThrow(new IllegalArgumentException("sourceCurrency and targetCurrency must be provided together"));

        mockMvc.perform(get("/api/v1/exchange-rates?sourceCurrency=USD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    @WithMockUser
    void hidesUnexpectedErrorsBehindAStandardResponse() throws Exception {
        when(exchangeRateService.history(eq(null), eq(null), any()))
                .thenThrow(new IllegalStateException("storage failure details"));

        mockMvc.perform(get("/api/v1/exchange-rates"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
