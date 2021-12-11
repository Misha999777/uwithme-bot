package education.uwithme.bot.client.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;

public class OauthClientConfig {

    @Bean
    public RequestInterceptor oauth2HttpRequestInterceptor(
            AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager) {
        return new AuthRequestInterceptor(authorizedClientManager);
    }

}
