package br.com.srm.creditengine.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class ApiSecurityConfigTest {

    @Test
    void mapsKeycloakRealmAndClientRolesToSpringAuthorities() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("scope", "read write")
                .claim("realm_access", Map.of("roles", List.of("operator")))
                .claim("resource_access", Map.of(
                        "srm-credit-engine-api",
                        Map.of("roles", List.of("settlement-reader"))))
                .build();

        var token = new ApiSecurityConfig()
                .jwtAuthenticationConverter("srm-credit-engine-api")
                .convert(jwt);

        assertThat(token).isNotNull();
        assertThat(token.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("SCOPE_read", "SCOPE_write", "ROLE_operator", "ROLE_settlement-reader");
    }

    @Test
    void acceptsTokensWithoutOptionalRoleContainers() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("sub", "operator")
                .build();

        var token = new ApiSecurityConfig()
                .jwtAuthenticationConverter("srm-credit-engine-api")
                .convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
    }
}
