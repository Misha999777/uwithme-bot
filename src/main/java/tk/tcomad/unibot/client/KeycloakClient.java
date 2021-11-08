package tk.tcomad.unibot.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import tk.tcomad.unibot.client.config.KeycloakClientConfig;
import tk.tcomad.unibot.dto.keycloak.AuthTokenRequest;
import tk.tcomad.unibot.dto.keycloak.KeycloakUserApi;
import tk.tcomad.unibot.dto.keycloak.LogoutRequest;
import tk.tcomad.unibot.dto.keycloak.TokenResponse;

@FeignClient(name = "KeycloakClient",
        url = "${keycloak.auth-server-url}/realms/${keycloak.realm}",
        configuration = KeycloakClientConfig.class)
public interface KeycloakClient {

    @PostMapping(value = "/protocol/openid-connect/token", consumes = "application/x-www-form-urlencoded")
    TokenResponse getToken(@RequestBody AuthTokenRequest request);

    @GetMapping("/protocol/openid-connect/userinfo")
    KeycloakUserApi getUser(@RequestHeader("authorization") String token);

    @PostMapping(value = "/protocol/openid-connect/logout", consumes = "application/x-www-form-urlencoded")
    void logoutUser(@RequestHeader("Authorization") String token, @RequestBody LogoutRequest logoutRequest);
}
