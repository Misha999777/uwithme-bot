package tk.tcomad.unibot.dto.uwithme;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.tcomad.unibot.telegram.Callback;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileApi implements BotData {

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
