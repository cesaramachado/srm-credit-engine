package br.com.srm.creditengine.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OpenApiConfigTest {

    @Test
    void publishesAuthorizationCodeFlowUsingTheConfiguredKeycloakUrl() {
        var openApi = new OpenApiConfig().creditEngineOpenApi("http://localhost:18180");
        var scheme = openApi.getComponents().getSecuritySchemes().get("keycloak-oauth2");

        assertThat(openApi.getInfo().getTitle()).isEqualTo("SRM Credit Engine API");
        assertThat(scheme.getFlows().getAuthorizationCode().getAuthorizationUrl())
                .isEqualTo("http://localhost:18180/realms/srm-credit-engine/protocol/openid-connect/auth");
        assertThat(scheme.getFlows().getAuthorizationCode().getTokenUrl())
                .isEqualTo("http://localhost:18180/realms/srm-credit-engine/protocol/openid-connect/token");
    }
}
