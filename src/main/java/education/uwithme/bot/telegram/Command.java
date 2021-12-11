package education.uwithme.bot.telegram;

import java.util.Arrays;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.util.Pair;

@Getter
@AllArgsConstructor
public enum Command {

    TIMETABLE(Pair.of("Расписание", "/gettimetable")),
    STUDENTS(Pair.of("Студенты", "/getstudents")),
    TEACHERS(Pair.of("Преподаватели", "/getteachers")),
    LECTURES(Pair.of("Лекции", "/getlectures")),
    TASKS(Pair.of("Задания", "/gettasks")),
    EXIT(Pair.of("Выйти", "/exit"));

    private final Pair<String, String> message;

    public static Optional<Command> fromMessage(String message) {
        return Arrays.stream(Command.values())
                     .filter(value -> value.getMessage().getSecond().equals(message))
                     .findFirst();
    }
}
