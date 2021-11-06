package tk.tcomad.unibot.client;

import java.util.List;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import tk.tcomad.unibot.client.config.UWithMeClientConfig;
import tk.tcomad.unibot.dto.uwithme.FileApi;
import tk.tcomad.unibot.dto.uwithme.LessonApi;
import tk.tcomad.unibot.dto.uwithme.UserApi;

@FeignClient(name = "KeycloakClient1",
        url = "${education.app.server.uri}",
        configuration = UWithMeClientConfig.class)
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
