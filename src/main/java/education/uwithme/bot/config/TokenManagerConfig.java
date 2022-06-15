package education.uwithme.bot.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.token.TokenManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TokenManagerConfig {

    @Bean
    public Keycloak keycloak(@Value("${keycloak.client.id}") String clientId,
                             @Value("${keycloak.client.secret}") String clientSecret,
                             @Value("${keycloak.realm}") String realm,
                             @Value("${keycloak.auth-server-url}") String serverUrl) {
        return KeycloakBuilder.builder()
                              .serverUrl(serverUrl)
                              .realm(realm)
                              .clientId(clientId)
                              .clientSecret(clientSecret)
                              .scope("openid")
                              .grantType("client_credentials")
                              .build();
    }

}
