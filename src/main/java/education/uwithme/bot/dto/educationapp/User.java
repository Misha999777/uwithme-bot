package education.uwithme.bot.dto.educationapp;

import com.mborodin.uwm.api.UserApi;
import com.mborodin.uwm.api.enums.Role;
import education.uwithme.bot.telegram.Callback;

import java.util.Objects;

public class User extends UserApi implements BotData {

    @Override
    public String getCallbackName() {
        return getRoles().contains(Role.ROLE_TEACHER)
                ? Callback.TEACHER.getMessage()
                : Callback.STUDENT.getMessage();
    }

    @Override
    public String getDataId() {
        return getId();
    }

    @Override
    public String getDisplayName() {
        return getFirstName() + " " + getSurname();
    }

    @Override
    public String getData() {
        return Objects.nonNull(getPhone())
                ? getEmail() + System.lineSeparator() + getPhone()
                : getEmail();
    }
}
