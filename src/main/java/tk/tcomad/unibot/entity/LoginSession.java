package tk.tcomad.unibot.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginSession {

    @Id
    private String stateToken;
    private String chatId;
    private String username;
    private String avatarUrl;
    private String userId;
}
