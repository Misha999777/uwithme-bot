package education.uwithme.bot.client;

import education.uwithme.bot.client.config.KeycloakClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import education.uwithme.bot.dto.keycloak.AuthTokenRequest;
import education.uwithme.bot.dto.keycloak.LogoutRequest;
import education.uwithme.bot.dto.keycloak.UserInfoResponse;
import education.uwithme.bot.dto.keycloak.TokenResponse;

@FeignClient(name = "KeycloakClient",
        url = "${keycloak.auth-server-url}/realms/${keycloak.realm}",
        configuration = KeycloakClientConfig.class)
public interface KeycloakClient {

    @PostMapping(value = "/protocol/openid-connect/token", consumes = "application/x-www-form-urlencoded")
    TokenResponse getToken(@RequestBody AuthTokenRequest request);

    @GetMapping("/protocol/openid-connect/userinfo")
    UserInfoResponse getUserInfo(@RequestHeader("Authorization") String authHeader);

    @PostMapping(value = "/protocol/openid-connect/logout", consumes = "application/x-www-form-urlencoded")
    void logout(@RequestBody LogoutRequest request);
}
