package education.uwithme.bot.dto.educationapp;

import education.uwithme.bot.telegram.Callback;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements BotData {

    private String id;
    private String firstName;
    private String surname;
    private String phone;
    private String email;
    private Role role;
    private Long studyGroupId;

    @Override
    public String getCallbackName() {
        return this.role.equals(Role.ROLE_TEACHER) ? Callback.TEACHER.getMessage() : Callback.STUDENT.getMessage();
    }

    @Override
    public String getDisplayName() {
        return this.firstName + " " + this.surname;
    }

    @Override
    public String getData() {
        return Objects.nonNull(this.phone)
                ? this.email + System.lineSeparator() + this.phone
                : this.email;
    }
}
