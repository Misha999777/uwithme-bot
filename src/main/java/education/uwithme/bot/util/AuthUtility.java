package education.uwithme.bot.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.keycloak.OAuth2Constants.AUTHORIZATION_CODE;
import static org.springframework.cloud.openfeign.security.OAuth2FeignRequestInterceptor.BEARER;

import java.net.URLEncoder;
import java.util.StringJoiner;

import education.uwithme.bot.client.KeycloakClient;
import education.uwithme.bot.dto.keycloak.AuthTokenRequest;
import education.uwithme.bot.dto.keycloak.UserInfoResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
@RequiredArgsConstructor
public class AuthUtility {

    private static final String TOKEN_PATH = "/token";
    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String RESPONSE_TYPE = "response_type";
    private static final String CODE = "code";
    private static final String SCOPE = "scope";
    private static final String OPENID = "openid";
    private static final String TOKEN = "token";

    @Value("${education-app.bot.uri}")
    private String redirectUri;
    @Value("${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect/auth")
    private String authUri;
    @Value("${keycloak.client.id}")
    private String client;
    @Value("${keycloak.client.secret}")
    private String secret;

    @NonNull
    private final KeycloakClient keycloakClient;

    public String constructLoginUri() {
        var params = new StringJoiner("&", "?", "")
                .add(constructParam(CLIENT_ID, client))
                .add(constructParam(REDIRECT_URI, URLEncoder.encode(constructRedirectUri(), UTF_8)))
                .add(constructParam(RESPONSE_TYPE, CODE))
                .add(constructParam(SCOPE, OPENID))
                .toString();

        return authUri.concat(params);
    }

    public void constructModel(Model model, String accessToken) {
        model.addAttribute(TOKEN, accessToken);
    }

    public UserInfoResponse getUserInfo(String code) {
        var request = new AuthTokenRequest(code, constructRedirectUri(),
                                           AUTHORIZATION_CODE, client, secret);

        var accessToken = keycloakClient.getToken(request)
                                        .getAccess_token();

        var headerValue = String.format("%s %s", BEARER, accessToken);

        return keycloakClient.getUserInfo(headerValue);
    }

    private String constructRedirectUri() {
        return redirectUri.concat(TOKEN_PATH);
    }

    private String constructParam(String name, String value) {
        return name.concat("=")
                   .concat(value);
    }
}
