package tk.tcomad.unibot.endpoint;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.keycloak.KeycloakSecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import tk.tcomad.unibot.client.EducationAppClient;
import tk.tcomad.unibot.client.KeycloakAdminClient;
import tk.tcomad.unibot.client.KeycloakClient;
import tk.tcomad.unibot.dto.keycloak.AuthTokenRequest;
import tk.tcomad.unibot.dto.keycloak.TokenResponse;
import tk.tcomad.unibot.dto.keycloak.UserSession;
import tk.tcomad.unibot.repository.BotUserRepository;
import tk.tcomad.unibot.telegram.TelegramBot;

@Controller
@RequiredArgsConstructor
public class AuthEndpoint {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    @Value("${spring.security.oauth2.client.provider.keycloak.authorization-uri}")
    private String authUri;
    @Value("${user.logout.uri}")
    private String logoutUri;
    @Value("${keycloak.resource}")
    private String client;
    @Value("${resource.id}")
    private String clientId;
    @Value("${user.login.redirect.uri}")
    private String redirectUri;
    @Value("${bot.key}")
    private String key;
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
    @Value("${education.app.frontend.uri}")
    private String frontendUri;

    private final BotUserRepository botUserRepository;
    private final TelegramBot telegramBot;
    private final KeycloakClient keycloakClient;
    private final EducationAppClient studentsClient;
    private final KeycloakAdminClient keycloakAdminClient;

    @GetMapping
    public void redirect(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Location", frontendUri);
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/login")
    @Transactional
    public void login(HttpServletResponse httpServletResponse, @RequestParam Map<String, String> request) {
        httpServletResponse.setHeader("Location", constructLoginUri(request));
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/token")
    public String token(Model model, @RequestParam Map<String, String> request) {
        var tokenResponse = getTokenResponseAndInvalidateDedicatedSessions(request);

        checkAuth(request);

        var botUser = botUserRepository.findById(Long.parseLong(request.get("id"))).orElseThrow();
        botUser.setUserId(tokenResponse.getUserId());
        botUser.setRefreshToken(tokenResponse.getRefresh_token());
        botUserRepository.save(botUser);

        model.addAttribute("username", request.get("username"));
        model.addAttribute("photo_url", request.get("photo_url"));
        model.addAttribute("token", tokenResponse.getAccess_token());
        model.addAttribute("userLink", "https://t.me/" + request.get("username"));
        model.addAttribute("logoutUri", constructLogoutUri());
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

    private String constructRedirectUri(Map<String, String> request) {
        return redirectUri.concat("/token")
                          .concat("?id=")
                          .concat(request.get("id"))
                          .concat("&first_name=")
                          .concat(request.get("first_name"))
                          .concat("&last_name=")
                          .concat(request.get("last_name"))
                          .concat("&username=")
                          .concat(request.get("username"))
                          .concat("&photo_url=")
                          .concat(request.get("photo_url"))
                          .concat("&auth_date=")
                          .concat(request.get("auth_date"))
                          .concat("&hash=")
                          .concat(request.get("hash"));
    }

    private String constructLoginUri(Map<String, String> request) {
        return authUri.concat("?client_id=")
                      .concat(client)
                      .concat("&redirect_uri=")
                      .concat(URLEncoder.encode(constructRedirectUri(request), StandardCharsets.UTF_8))
                      .concat("&response_type=code")
                      .concat("&scope=openid");
    }

    private String constructLogoutUri() {
        return logoutUri.concat("?redirect_uri=")
                        .concat(redirectUri)
                        .concat("/close");
    }

    private TokenResponse getTokenResponseAndInvalidateDedicatedSessions(Map<String, String> request) {
        var redirect = constructRedirectUri(request);
        var code = request.get("code");

        var token = keycloakClient.getToken(new AuthTokenRequest(code,
                                                                 redirect,
                                                                 client,
                                                                 "authorization_code",
                                                                 clientSecret));
        var userId = keycloakClient.getUserInfo("Bearer " + token.getAccess_token()).getSub();
        keycloakAdminClient.getUserSessions(clientId).stream()
                           .filter(userSession -> userSession.getUserId().equals(userId))
                           .filter(userSession -> userSession.getClients().containsValue(client))
                           .filter(userSession -> Objects.equals(userSession.getStart(), userSession.getLastAccess()))
                           .map(UserSession::getId)
                           .forEach(keycloakAdminClient::revokeUserSession);

        token.setUserId(userId);
        return token;
    }

    @SneakyThrows
    private void checkAuth(@RequestBody Map<String, String> request) {
        request.remove("code");
        String hash = request.get("hash");
        request.remove("hash");

        String str = request.entrySet().stream()
                            .sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
                            .map(kvp -> kvp.getKey() + "=" + kvp.getValue())
                            .collect(Collectors.joining("\n"));

        var spec = new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8)),
                                     "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(spec);

        var result = mac.doFinal(str.getBytes(StandardCharsets.UTF_8));
        String resultStr = bytesToHex(result);

        if (hash.compareToIgnoreCase(resultStr) != 0) {
            throw new RuntimeException();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        var hexChars = new char[bytes.length * 2];
        for (var j = 0; j < bytes.length; j++) {
            var v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
