package education.uwithme.bot.client;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import education.uwithme.bot.client.config.KeycloakClientConfig;
import education.uwithme.bot.dto.keycloak.AuthTokenRequest;
import education.uwithme.bot.dto.keycloak.TokenResponse;
import education.uwithme.bot.dto.keycloak.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "KeycloakClient",
        url = "${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect",
        configuration = KeycloakClientConfig.class)
public interface KeycloakClient {

    @PostMapping(value = "/token",
            consumes = "application/x-www-form-urlencoded")
    TokenResponse getToken(@RequestBody AuthTokenRequest request);

    @GetMapping(value = "/userinfo")
    UserInfoResponse getUserInfo(@RequestHeader(AUTHORIZATION) String authorizationHeader);
}
