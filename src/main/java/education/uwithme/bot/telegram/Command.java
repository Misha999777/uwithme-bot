package education.uwithme.bot.telegram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.util.Pair;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Command {

    TIMETABLE(Pair.of("Розклад", "/gettimetable")),
    STUDENTS(Pair.of("Студенти", "/getstudents")),
    TEACHERS(Pair.of("Викладачі", "/getteachers")),
    LECTURES(Pair.of("Лекції", "/getlectures")),
    TASKS(Pair.of("Завдання", "/gettasks")),
    EXIT(Pair.of("Вийти", "/exit"));

    private final Pair<String, String> message;

    public static Optional<Command> fromMessage(String message) {
        return Arrays.stream(Command.values())
                .filter(value -> value.getMessage().getSecond().equals(message))
                .findFirst();
    }
}
