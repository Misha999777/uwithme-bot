package tk.tcomad.unibot.endpoint;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.springframework.cloud.openfeign.security.OAuth2FeignRequestInterceptor.BEARER;

import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import tk.tcomad.unibot.client.EducationAppClient;
import tk.tcomad.unibot.client.KeycloakClient;
import tk.tcomad.unibot.dto.keycloak.AuthTokenRequest;
import tk.tcomad.unibot.entity.BotUser;
import tk.tcomad.unibot.repository.BotUserRepository;
import tk.tcomad.unibot.telegram.TelegramBot;

@Controller
@RequiredArgsConstructor
public class BotEndpoint {

    private static final String GRANT = "authorization_code";

    @Value("${spring.security.oauth2.client.provider.keycloak.authorization-uri}")
    private String authUri;
    @Value("${user.logout.uri}")
    private String logoutUri;
    @Value("${user.login.resource}")
    private String client;
    @Value("${user.login.redirect.uri}")
    private String redirectUri;


    private final BotUserRepository botUserRepository;
    private final TelegramBot telegramBot;
    private final KeycloakClient keycloakClient;
    private final EducationAppClient studentsClient;

    @GetMapping("/login")
    public void login(HttpServletResponse httpServletResponse, @RequestParam String state) {
        if (botUserRepository.existsById(Long.parseLong(state))) {
            botUserRepository.deleteById(Long.parseLong(state));
            httpServletResponse.setHeader("Location", constructLogoutUri(state));
        } else {
            httpServletResponse.setHeader("Location", constructLoginUri(state));
        }
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/token")
    public ModelAndView login(@RequestParam String state, @RequestParam String code) {
        var token = keycloakClient.getToken(new AuthTokenRequest(code, redirectUri + "/token", client, GRANT));
        var user = keycloakClient.getUser(BEARER + SPACE + token.getAccess_token());
        try {
            var uWithMeUser = studentsClient.getUser(user.getSub());
            Objects.requireNonNull(uWithMeUser);
            Objects.requireNonNull(uWithMeUser.getStudyGroupId());
            var botUser = new BotUser(Long.parseLong(state), uWithMeUser.getStudyGroupId());
            botUserRepository.save(botUser);
            telegramBot.onLoginComplete(Long.parseLong(state), user.getName());
        } catch (Exception ignored) {
            var botUser = new BotUser(Long.parseLong(state), null);
            botUserRepository.save(botUser);
            telegramBot.onLoginFail(Long.parseLong(state));
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login.html");
        return modelAndView;
    }

    private String constructLoginUri(String state) {
        return new StringBuilder().append(authUri)
                                  .append("?client_id=")
                                  .append(client)
                                  .append("&redirect_uri=")
                                  .append(redirectUri)
                                  .append("/token")
                                  .append("&response_type=code")
                                  .append("&scope=openid")
                                  .append("&state=")
                                  .append(state)
                                  .toString();
    }

    private String constructLogoutUri(String state) {
        return new StringBuilder().append(logoutUri)
                                  .append("?redirect_uri=")
                                  .append(redirectUri)
                                  .append("/login")
                                  .append("&state=")
                                  .append(state)
                                  .toString();
    }

}
