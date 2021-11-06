package tk.tcomad.unibot.dto.uwithme;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.tcomad.unibot.telegram.Callback;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserApi implements BotData {

    private String id;
    private String firstName;
    private String lastName;
    private String surname;
    private String phone;
    private String email;
    private Role role;
    private String studyGroupName;
    private Long studyGroupId;
    private String instituteName;
    private String departmentName;
    private Long universityId;
    private Boolean isAdmin;

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
        return this.email;
    }

}
