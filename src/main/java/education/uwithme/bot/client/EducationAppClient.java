package education.uwithme.bot.client;

import education.uwithme.bot.config.OauthClientConfig;
import education.uwithme.bot.dto.File;
import education.uwithme.bot.dto.Lesson;
import education.uwithme.bot.dto.User;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "KeycloakClient1",
        url = "${education-app.server.uri}",
        configuration = OauthClientConfig.class)
public interface EducationAppClient {

    @RequestMapping(method = RequestMethod.GET, value = "/users")
    List<User> getStudents(@RequestParam("groupId") Long groupId);

    @RequestMapping(method = RequestMethod.GET, value = "/users/teachers/groupId/{groupId}")
    List<User> getTeachers(@PathVariable("groupId") Long groupId);

    @RequestMapping(method = RequestMethod.GET, value = "/lessons")
    List<Lesson> getLessons(@RequestParam("groupId") Long groupId);

    @RequestMapping(method = RequestMethod.GET, value = "/files/groupId/{groupId}")
    List<File> getFiles(@PathVariable("groupId") Long groupId);

    @RequestMapping(method = RequestMethod.GET, value = "/files/{fileId}")
    Response getFile(@PathVariable("fileId") Long fileId);

    @RequestMapping(method = RequestMethod.GET, value = "/users/{userId}")
    User getUser(@PathVariable("userId") String userId);
}
