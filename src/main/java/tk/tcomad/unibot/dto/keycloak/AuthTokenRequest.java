package tk.tcomad.unibot.dto.keycloak;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenRequest {

    private String code;
    private String redirect_uri;
    private String client_id;
    private String grant_type;
    private String client_secret;
}
