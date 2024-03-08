package education.uwithme.bot.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
