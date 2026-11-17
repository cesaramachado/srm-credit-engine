package br.com.srm.creditengine.receivable.api;

import br.com.srm.creditengine.receivable.application.ReceivableApplicationService;
import br.com.srm.creditengine.receivable.persistence.ReceivableEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/receivables")
@Tag(name = "Receivables")
@SecurityRequirement(name = "keycloak-oauth2")
public class ReceivableController {

    private final ReceivableApplicationService receivableService;
    private final ReceivableMapper receivableMapper;

    public ReceivableController(ReceivableApplicationService receivableService, ReceivableMapper receivableMapper) {
        this.receivableService = receivableService;
        this.receivableMapper = receivableMapper;
    }

    @PostMapping
    @Operation(summary = "Register a receivable")
    public ResponseEntity<ReceivableResponse> create(@Valid @RequestBody ReceivableCreateRequest request) {
        ReceivableEntity created = receivableService.create(
                request.assignor(),
                request.faceValueBrl(),
                request.dueDate(),
                request.receivableType());
        return ResponseEntity.created(URI.create("/api/v1/receivables/" + created.getId()))
                .body(receivableMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a receivable by identifier")
    public ReceivableResponse get(@PathVariable UUID id) {
        return receivableMapper.toResponse(receivableService.get(id));
    }
}
