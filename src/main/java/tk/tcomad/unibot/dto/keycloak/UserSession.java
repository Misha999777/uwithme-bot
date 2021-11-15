package tk.tcomad.unibot.dto.keycloak;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {

    String id;
    String userId;
    Long start;
    Long lastAccess;
    Map<String, String> clients;

}
