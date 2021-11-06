package tk.tcomad.unibot.telegram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@AllArgsConstructor
public enum Day {

    MONDAY(Pair.of("Понедельник", "/Day 1")),
    TUESDAY(Pair.of("Вторник", "/Day 2")),
    WEDNESDAY(Pair.of("Среда", "/Day 3")),
    THURSDAY(Pair.of("Четверг", "/Day 4")),
    FRIDAY(Pair.of("Пятница", "/Day 5")),
    SATURDAY(Pair.of("Суббота", "/Day 6"));

    private final Pair<String, String> message;
}
