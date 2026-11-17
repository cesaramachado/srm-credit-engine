package br.com.srm.creditengine.receivable.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.srm.creditengine.common.exception.ApiExceptionHandler;
import br.com.srm.creditengine.config.ApiSecurityConfig;
import br.com.srm.creditengine.pricing.domain.ReceivableType;
import br.com.srm.creditengine.receivable.application.ReceivableApplicationService;
import br.com.srm.creditengine.receivable.domain.ReceivableStatus;
import br.com.srm.creditengine.receivable.persistence.ReceivableEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(ReceivableController.class)
@Import({ApiExceptionHandler.class, ApiSecurityConfig.class})
class ReceivableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReceivableApplicationService receivableService;

    @MockitoBean
    private ReceivableMapper receivableMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void requiresAuthenticationForBusinessEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/receivables")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void registersAValidatedReceivable() throws Exception {
        ReceivableEntity entity = new ReceivableEntity(
                "cedente-teste",
                new BigDecimal("100000.00"),
                LocalDate.of(2027, 2, 15),
                ReceivableType.DUPLICATA_MERCANTIL);
        ReceivableResponse response = new ReceivableResponse(
                entity.getId(),
                "cedente-teste",
                new BigDecimal("100000.00"),
                LocalDate.of(2027, 2, 15),
                ReceivableType.DUPLICATA_MERCANTIL,
                ReceivableStatus.OPEN,
                0,
                Instant.parse("2026-11-17T12:00:00Z"));
        when(receivableService.create(
                eq("cedente-teste"),
                eq(new BigDecimal("100000.00")),
                eq(LocalDate.of(2027, 2, 15)),
                eq(ReceivableType.DUPLICATA_MERCANTIL)))
                .thenReturn(entity);
        when(receivableMapper.toResponse(any(ReceivableEntity.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/receivables")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "assignor": "cedente-teste",
                                  "faceValueBrl": 100000.00,
                                  "dueDate": "2027-02-15",
                                  "receivableType": "DUPLICATA_MERCANTIL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/receivables/" + entity.getId()))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.faceValueBrl").value(100000.0));
    }

    @Test
    @WithMockUser
    void rejectsInvalidReceivableInput() throws Exception {
        mockMvc.perform(post("/api/v1/receivables")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "assignor": " ",
                                  "faceValueBrl": 0,
                                  "dueDate": null,
                                  "receivableType": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser
    void getsAnExistingReceivable() throws Exception {
        UUID id = UUID.randomUUID();
        ReceivableEntity entity = new ReceivableEntity(
                "cedente-teste", new BigDecimal("100.00"), LocalDate.of(2027, 2, 15),
                ReceivableType.CHEQUE_PRE_DATADO);
        when(receivableService.get(id)).thenReturn(entity);
        when(receivableMapper.toResponse(entity)).thenReturn(new ReceivableResponse(
                entity.getId(), entity.getAssignor(), entity.getFaceValueBrl(), entity.getDueDate(),
                entity.getReceivableType(), entity.getStatus(), 0, entity.getCreatedAt()));

        mockMvc.perform(get("/api/v1/receivables/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receivableType").value("CHEQUE_PRE_DATADO"));
    }

    @Test
    @WithMockUser
    void returnsNotFoundForAnUnknownReceivable() throws Exception {
        UUID id = UUID.randomUUID();
        when(receivableService.get(id)).thenThrow(
                new br.com.srm.creditengine.common.exception.ReceivableNotFoundException(id));

        mockMvc.perform(get("/api/v1/receivables/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RECEIVABLE_NOT_FOUND"));
    }
}
