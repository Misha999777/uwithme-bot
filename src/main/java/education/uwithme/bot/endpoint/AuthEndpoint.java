package education.uwithme.bot.endpoint;

import static org.keycloak.OAuth2Constants.AUTHORIZATION_CODE;
import static org.springframework.cloud.openfeign.security.OAuth2FeignRequestInterceptor.BEARER;
import static org.springframework.http.HttpHeaders.LOCATION;

import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import education.uwithme.bot.util.StringUtility;
import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakSecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import education.uwithme.bot.client.EducationAppClient;
import education.uwithme.bot.client.KeycloakClient;
import education.uwithme.bot.dto.keycloak.AuthTokenRequest;
import education.uwithme.bot.dto.keycloak.TokenResponse;
import education.uwithme.bot.repository.BotUserRepository;
import education.uwithme.bot.telegram.TelegramBot;
import education.uwithme.bot.util.SHA256Utility;

@Controller
@RequiredArgsConstructor
public class AuthEndpoint {

    @Value("${keycloak.resource}")
    private String client;
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
    @Value("${education.app.frontend.uri}")
    private String frontendUri;

    private final BotUserRepository botUserRepository;
    private final TelegramBot telegramBot;
    private final KeycloakClient keycloakClient;
    private final EducationAppClient studentsClient;
    private final StringUtility stringUtility;
    private final SHA256Utility sha256Utility;

    @GetMapping
    public void redirect(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader(LOCATION, frontendUri);
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/login")
    @Transactional
    public void login(HttpServletResponse httpServletResponse, @RequestParam Map<String, String> request) {
        httpServletResponse.setHeader(LOCATION, stringUtility.constructLoginUri(request));
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/token")
    public String token(Model model, @RequestParam Map<String, String> request) {
        var tokenResponse = getTokenResponseAndInvalidateDedicatedSessions(request);

        sha256Utility.checkTelegramData(request);

        var botUser = botUserRepository.findById(Long.parseLong(request.get("id"))).orElseThrow();
        botUser.setUserId(tokenResponse.getUserId());
        botUserRepository.save(botUser);

        stringUtility.constructModel(model, request, tokenResponse.getAccess_token());
        return "login.html";
    }

    @GetMapping("/complete")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void complete(HttpServletRequest request) {
        var context = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        var token = context.getToken();
        if (!Objects.equals(token.getIssuedFor(), client)) {
            throw new RuntimeException();
        }

        var botUser = botUserRepository.findBotUserByUserId(token.getSubject()).orElseThrow();

        try {
            var educationAppUser = studentsClient.getUser(token.getSubject());
            Objects.requireNonNull(educationAppUser);
            Objects.requireNonNull(educationAppUser.getStudyGroupId());

            botUser.setGroupId(educationAppUser.getStudyGroupId());
            botUserRepository.save(botUser);

            telegramBot.onLoginComplete(botUser.getChatId(), educationAppUser.getFirstName());
        } catch (Exception ignored) {
            telegramBot.onLoginFail(botUser.getChatId());
        }
    }

    @GetMapping("/close")
    public ModelAndView close() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("close.html");
        return modelAndView;
    }

    private TokenResponse getTokenResponseAndInvalidateDedicatedSessions(Map<String, String> request) {
        var redirect = stringUtility.constructRedirectUri(request);
        var code = request.get("code");

        var token = keycloakClient.getToken(new AuthTokenRequest(code,
                                                                 redirect,
                                                                 client,
                                                                 AUTHORIZATION_CODE,
                                                                 clientSecret));
        var userId = keycloakClient.getUserInfo(BEARER + " " + token.getAccess_token()).getSub();

        token.setUserId(userId);
        return token;
    }
}
