package tk.tcomad.unibot.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotUser implements Serializable {

    @Id
    private Long chatId;
    private String userId;
    private Long groupId;
}
