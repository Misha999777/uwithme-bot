package education.uwithme.bot.client;

import java.util.List;

import education.uwithme.bot.client.config.OauthClientConfig;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import education.uwithme.bot.dto.educationapp.FileApi;
import education.uwithme.bot.dto.educationapp.LessonApi;
import education.uwithme.bot.dto.educationapp.UserApi;

@FeignClient(name = "KeycloakClient1",
        url = "${education.app.server.uri}",
        configuration = OauthClientConfig.class)
public interface EducationAppClient {

    @RequestMapping(method = RequestMethod.GET, value = "/users/students/groupId/{groupId}")
    List<UserApi> getStudents(@PathVariable("groupId") Long groupId);

    @RequestMapping(method = RequestMethod.GET, value = "/users/teachers/groupId/{groupId}")
    List<UserApi> getTeachers(@PathVariable("groupId") Long groupId);

    @RequestMapping(method = RequestMethod.GET, value = "/lessons/group/{groupId}")
    List<LessonApi> getLessons(@PathVariable("groupId") Long groupId);

    @RequestMapping(method = RequestMethod.GET, value = "/files/groupId/{groupId}")
    List<FileApi> getFiles(@PathVariable("groupId") Long groupId);

    @RequestMapping(method = RequestMethod.GET, value = "/files/{fileId}")
    Response getFile(@PathVariable("fileId") Long fileId);

    @RequestMapping(method = RequestMethod.GET, value = "/users/{userId}")
    UserApi getUser(@PathVariable("userId") String userId);

}
