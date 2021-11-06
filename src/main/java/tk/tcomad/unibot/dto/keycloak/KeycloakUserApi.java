package tk.tcomad.unibot.dto.keycloak;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakUserApi {

    String sub;
    String name;
}
