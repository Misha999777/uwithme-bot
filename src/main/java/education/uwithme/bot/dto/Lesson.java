package education.uwithme.bot.dto;

import com.mborodin.uwm.api.LessonApi;
import education.uwithme.bot.telegram.Callback;

import java.util.Map;

public class Lesson extends LessonApi implements BotData {

    private static final String TEACHER_PREFIX = "Викладач: ";
    private static final String BUILDING_PREFIX = "Корпус: ";
    private static final String HALL_PREFIX = "Аудиторія: ";
    private static final Map<Long, String> timeMap = Map.of(1L, "8:30",
            2L, "10:25",
            3L, "12:35",
            4L, "14:30",
            5L, "16:25");

    @Override
    public String getCallbackName() {
        return Callback.DAY.getMessage();
    }

    @Override
    public String getDataId() {
        return getId().toString();
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getData() {
        return new StringBuilder()
                .append(timeMap.get(getLessonTime()))
                .append(" ")
                .append(getSubjectName())
                .append(System.lineSeparator())
                .append(TEACHER_PREFIX)
                .append(getTeacherName())
                .append(System.lineSeparator())
                .append(BUILDING_PREFIX)
                .append(getBuilding())
                .append(System.lineSeparator())
                .append(HALL_PREFIX)
                .append(getLectureHall())
                .append(System.lineSeparator())
                .toString();
    }
}
