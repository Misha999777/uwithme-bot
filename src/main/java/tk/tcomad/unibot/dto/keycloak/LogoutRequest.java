package tk.tcomad.unibot.dto.keycloak;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequest {

    private String refresh_token;
    private String client_id;
    private String client_secret;
}
