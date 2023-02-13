package education.uwithme.bot.dto.educationapp;

import education.uwithme.bot.telegram.Callback;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class File implements BotData {

    private Long fileId;
    private String fileName;
    private Integer type;
    private Long subjectId;
    private Date timeStartAccess;

    @Override
    public String getCallbackName() {
        return type == 1 ? Callback.LECTURE.getMessage() : Callback.TASK.getMessage();
    }

    @Override
    public String getDisplayName() {
        return this.fileName;
    }

    @Override
    public String getData() {
        return fileId.toString();
    }

    @Override
    public String getId() {
        return fileId.toString();
    }
}
