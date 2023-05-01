package education.uwithme.bot.dto.educationapp;

import com.mborodin.uwm.api.FileApi;
import com.mborodin.uwm.api.enums.FileType;
import education.uwithme.bot.telegram.Callback;

public class File extends FileApi implements BotData {

    @Override
    public String getCallbackName() {
        return getFileType() == FileType.LECTURE
                ? Callback.LECTURE.getMessage()
                : Callback.TASK.getMessage();
    }

    @Override
    public String getDataId() {
        return String.valueOf(getFileId());
    }

    @Override
    public String getDisplayName() {
        return getFileName();
    }

    @Override
    public String getData() {
        return String.valueOf(getFileId());
    }
}
