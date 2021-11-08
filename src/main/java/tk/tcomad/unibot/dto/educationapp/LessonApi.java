package tk.tcomad.unibot.dto.educationapp;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import tk.tcomad.unibot.telegram.Callback;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonApi implements BotData {

    transient private final static Map<Long, String> timeMap = Map.of(1L, "8:30",
                                                                      2L, "10:25",
                                                                      3L, "12:35",
                                                                      4L, "14:30",
                                                                      5L, "16:25");

    private Long id;
    private String subjectName;
    private String teacherName;
    private String lectureHall;
    private String building;
    private List<String> groups;
    private Long weekDay;
    private Long lessonTime;
    private Long weekNumber;

    @Override
    public String getCallbackName() {
        return Callback.DAY.getMessage();
    }

    @Override
    public String getDisplayName() {
        throw new NotImplementedException();
    }

    @Override
    public String getData() {
        return new StringBuilder()
                .append(timeMap.get(lessonTime))
                .append(" ")
                .append(subjectName)
                .append(System.lineSeparator())
                .append("Преподаватель: ")
                .append(teacherName)
                .append(System.lineSeparator())
                .append("Корпус: ")
                .append(building)
                .append(System.lineSeparator())
                .append("Аудитория: ")
                .append(lectureHall)
                .append(System.lineSeparator())
                .toString();
    }

    @Override
    public String getId() {
        return id.toString();
    }
}
