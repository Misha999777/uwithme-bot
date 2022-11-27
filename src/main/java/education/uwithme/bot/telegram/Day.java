package education.uwithme.bot.telegram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.util.Pair;

@Getter
@AllArgsConstructor
public enum Day {

    MONDAY(Pair.of("Понеділок", "/Day 1")),
    TUESDAY(Pair.of("Вівторок", "/Day 2")),
    WEDNESDAY(Pair.of("Середа", "/Day 3")),
    THURSDAY(Pair.of("Четвер", "/Day 4")),
    FRIDAY(Pair.of("Пʼятниця", "/Day 5")),
    SATURDAY(Pair.of("Субота", "/Day 6"));

    private final Pair<String, String> message;
}
