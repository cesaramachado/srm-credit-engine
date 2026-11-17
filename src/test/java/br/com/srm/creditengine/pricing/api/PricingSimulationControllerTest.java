package br.com.srm.creditengine.pricing.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.srm.creditengine.common.exception.ApiExceptionHandler;
import br.com.srm.creditengine.config.ApiSecurityConfig;
import br.com.srm.creditengine.pricing.application.PricingSimulationApplicationService;
import br.com.srm.creditengine.pricing.domain.CurrencyCode;
import br.com.srm.creditengine.pricing.domain.PricingResult;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PricingSimulationController.class)
@Import({ApiExceptionHandler.class, ApiSecurityConfig.class})
class PricingSimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PricingSimulationApplicationService pricingService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void requiresAuthenticationForPricingSimulation() throws Exception {
        mockMvc.perform(post("/api/v1/pricing/simulations")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void doesNotAcceptBasicAuthenticationForBusinessEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/pricing/simulations")
                        .with(httpBasic("metrics", "not-an-api-credential"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void rejectsInvalidSimulationInput() throws Exception {
        mockMvc.perform(post("/api/v1/pricing/simulations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "faceValueBrl": 0,
                                  "termMonths": 0,
                                  "baseMonthlyRate": null,
                                  "receivableType": null,
                                  "paymentCurrency": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser
    void returnsThePricingResultForAValidSimulation() throws Exception {
        when(pricingService.simulate(any())).thenReturn(new PricingResult(
                new BigDecimal("92859.94"),
                new BigDecimal("7140.06"),
                new BigDecimal("17094.67"),
                CurrencyCode.USD,
                new BigDecimal("0.01"),
                new BigDecimal("0.015"),
                3,
                new BigDecimal("5.4321")));

        mockMvc.perform(post("/api/v1/pricing/simulations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "faceValueBrl": 100000.00,
                                  "termMonths": 3,
                                  "baseMonthlyRate": 0.01,
                                  "receivableType": "DUPLICATA_MERCANTIL",
                                  "paymentCurrency": "USD",
                                  "brlPerUsd": 5.4321
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presentValueBrl").value(92859.94))
                .andExpect(jsonPath("$.settlementAmount").value(17094.67));
    }

    @Test
    @WithMockUser
    void mapsPricingRuleViolationsToBadRequest() throws Exception {
        when(pricingService.simulate(any())).thenThrow(new IllegalArgumentException("monthly factor is invalid"));

        mockMvc.perform(post("/api/v1/pricing/simulations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "faceValueBrl": 100000.00,
                                  "termMonths": 3,
                                  "baseMonthlyRate": -1,
                                  "receivableType": "DUPLICATA_MERCANTIL",
                                  "paymentCurrency": "BRL"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }
}
