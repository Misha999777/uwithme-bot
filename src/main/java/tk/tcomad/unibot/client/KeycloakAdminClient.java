package tk.tcomad.unibot.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tk.tcomad.unibot.client.config.OauthClientConfig;
import tk.tcomad.unibot.dto.keycloak.UserSession;

@FeignClient(name = "KeycloakAdminClient",
        url = "${keycloak.auth-server-url}/admin/realms/${keycloak.realm}",
        configuration = OauthClientConfig.class)
public interface KeycloakAdminClient {

    @GetMapping("/clients/{clientId}/user-sessions")
    List<UserSession> getUserSessions(@PathVariable final String clientId);

    @DeleteMapping("/sessions/{session}")
    void revokeUserSession(@PathVariable final String session);

}
