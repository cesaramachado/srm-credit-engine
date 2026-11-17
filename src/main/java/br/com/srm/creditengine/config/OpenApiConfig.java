package br.com.srm.creditengine.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI creditEngineOpenApi(
            @Value("${keycloak.public-url:http://localhost:18180}") String keycloakPublicUrl) {
        return new OpenAPI()
                .info(new Info()
                        .title("SRM Credit Engine API")
                        .version("v1")
                        .description("API para simulação de preço, recebíveis e histórico de cotações."))
                .components(new Components()
                        .addSecuritySchemes("keycloak-oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                                        .authorizationUrl(keycloakPublicUrl
                                                + "/realms/srm-credit-engine/protocol/openid-connect/auth")
                                        .tokenUrl(keycloakPublicUrl
                                                + "/realms/srm-credit-engine/protocol/openid-connect/token")
                                        .scopes(new Scopes().addString("openid", "OpenID Connect"))))));
    }
}
