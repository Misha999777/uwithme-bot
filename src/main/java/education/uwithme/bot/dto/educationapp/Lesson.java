package education.uwithme.bot.dto.educationapp;

import education.uwithme.bot.telegram.Callback;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Lesson implements BotData {

    private static final String TEACHER_PREFIX = "Викладач: ";
    private static final String BUILDING_PREFIX = "Корпус: ";
    private static final String HALL_PREFIX = "Аудиторія: ";
    private static final Map<Long, String> timeMap = Map.of(1L, "8:30",
            2L, "10:25",
            3L, "12:35",
            4L, "14:30",
            5L, "16:25");

    private Long id;
    private String subjectName;
    private String teacherName;
    private String lectureHall;
    private String building;
    private Long weekDay;
    private Long lessonTime;
    private Long weekNumber;

    @Override
    public String getCallbackName() {
        return Callback.DAY.getMessage();
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getData() {
        return new StringBuilder()
                .append(timeMap.get(lessonTime))
                .append(" ")
                .append(subjectName)
                .append(System.lineSeparator())
                .append(TEACHER_PREFIX)
                .append(teacherName)
                .append(System.lineSeparator())
                .append(BUILDING_PREFIX)
                .append(building)
                .append(System.lineSeparator())
                .append(HALL_PREFIX)
                .append(lectureHall)
                .append(System.lineSeparator())
                .toString();
    }

    @Override
    public String getId() {
        return id.toString();
    }
}
