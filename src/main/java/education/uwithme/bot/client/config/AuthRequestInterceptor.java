package education.uwithme.bot.client.config;

import static org.springframework.cloud.openfeign.security.OAuth2FeignRequestInterceptor.BEARER;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.Objects;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

public class AuthRequestInterceptor implements RequestInterceptor {

    @Value("${spring.security.oauth2.client.registration.keycloak.provider}")
    private String registrationId;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String principal;

    private final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager;

    public AuthRequestInterceptor(AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    @Override
    public void apply(final RequestTemplate requestTemplate) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
                                                                        .principal(principal)
                                                                        .build();
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (Objects.nonNull(authorizedClient)) {
            requestTemplate.header(AUTHORIZATION,
                                   BEARER + " " + authorizedClient.getAccessToken().getTokenValue());
        }
    }
}
