package education.uwithme.bot.client;

import education.uwithme.bot.client.config.KeycloakClientConfig;
import education.uwithme.bot.dto.keycloak.AuthTokenRequest;
import education.uwithme.bot.dto.keycloak.TokenResponse;
import education.uwithme.bot.dto.keycloak.UserInfoResponse;
import feign.Headers;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "KeycloakClient",
        url = "${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect",
        configuration = KeycloakClientConfig.class)
public interface KeycloakClient {

    @PostMapping(value = "/token",
            consumes = "application/x-www-form-urlencoded")
    TokenResponse getToken(@RequestBody AuthTokenRequest request);

    @GetMapping(value = "/userinfo")
    @Headers("Authorization: Bearer {token}")
    UserInfoResponse getUserInfo(@Param("token") String token);
}
