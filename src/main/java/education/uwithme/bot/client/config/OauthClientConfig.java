package education.uwithme.bot.client.config;

import static org.springframework.cloud.openfeign.security.OAuth2FeignRequestInterceptor.BEARER;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;
import org.springframework.context.annotation.Bean;

public class OauthClientConfig {

    @Bean
    public RequestInterceptor oauth2HttpRequestInterceptor(Keycloak keycloak) {
        return requestTemplate -> setAuthHeader(requestTemplate, keycloak.tokenManager());
    }

    private void setAuthHeader(RequestTemplate requestTemplate, TokenManager tokenManager) {
        var accessToken = tokenManager.getAccessTokenString();
        var headerValue = String.format("%s %s", BEARER, accessToken);

        requestTemplate.header(AUTHORIZATION, headerValue);
    }
}
