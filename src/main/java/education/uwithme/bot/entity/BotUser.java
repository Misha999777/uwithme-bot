package education.uwithme.bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotUser implements Serializable {

    @Id
    private Long chatId;
    private String userId;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> loginMessageIds = new ArrayList<>();
}
