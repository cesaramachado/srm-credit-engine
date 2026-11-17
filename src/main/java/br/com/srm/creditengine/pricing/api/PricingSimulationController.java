package br.com.srm.creditengine.pricing.api;

import br.com.srm.creditengine.pricing.application.PricingSimulationApplicationService;
import br.com.srm.creditengine.pricing.domain.PricingRequest;
import br.com.srm.creditengine.pricing.domain.PricingResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pricing/simulations")
@Tag(name = "Pricing")
@SecurityRequirement(name = "keycloak-oauth2")
public class PricingSimulationController {

    private final PricingSimulationApplicationService pricingService;

    public PricingSimulationController(PricingSimulationApplicationService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping
    @Operation(summary = "Simulates a receivable price using the supplied rates and term")
    public ResponseEntity<PricingResult> simulate(@Valid @RequestBody PricingSimulationRequest request) {
        PricingResult result = pricingService.simulate(new PricingRequest(
                request.faceValueBrl(),
                request.termMonths(),
                request.baseMonthlyRate(),
                request.receivableType(),
                request.paymentCurrency(),
                request.brlPerUsd()));
        return ResponseEntity.ok(result);
    }
}
