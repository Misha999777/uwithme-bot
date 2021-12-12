package education.uwithme.bot.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class StringUtility {

    private static final String ID = "id";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String USERNAME = "username";
    private static final String PHOTO_URL = "photo_url";
    private static final String AUTH_DATE = "auth_date";
    private static final String HASH = "hash";
    private static final String TOKEN_PATH = "/token";
    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String RESPONSE_TYPE = "response_type";
    private static final String CODE = "code";
    private static final String SCOPE = "scope";
    private static final String OPENID = "openid";
    private static final String T_ME = "https://t.me/";
    private static final String USER_URL = "user_url";
    private static final String TOKEN = "token";

    @Value("${spring.security.oauth2.client.provider.keycloak.authorization-uri}")
    private String authUri;
    @Value("${user.login.redirect.uri}")
    private String redirectUri;
    @Value("${keycloak.resource}")
    private String client;

    public String constructRedirectUri(Map<String, String> request) {

        var params = new StringJoiner("&", "?", "")
                .add(constructParam(ID, request.get(ID)))
                .add(constructParam(FIRST_NAME, request.get(FIRST_NAME)))
                .add(constructParam(USERNAME, request.get(USERNAME)))
                .add(constructParam(AUTH_DATE, request.get(AUTH_DATE)))
                .add(constructParam(HASH, request.get(HASH)));

        Optional.ofNullable(request.get(LAST_NAME))
                .ifPresent(lastName -> params.add(constructParam(LAST_NAME, lastName)));
        Optional.ofNullable(request.get(PHOTO_URL))
                .ifPresent(photoUrl -> params.add(constructParam(PHOTO_URL, photoUrl)));

        return redirectUri.concat(TOKEN_PATH)
                          .concat(params.toString());
    }

    public String constructLoginUri(Map<String, String> request) {
        var params = new StringJoiner("&", "?", "")
                .add(constructParam(CLIENT_ID, client))
                .add(constructParam(REDIRECT_URI, URLEncoder.encode(constructRedirectUri(request),
                                                                    StandardCharsets.UTF_8)))
                .add(constructParam(RESPONSE_TYPE, CODE))
                .add(constructParam(SCOPE, OPENID))
                .toString();

        return authUri.concat(params);
    }

    public void constructModel(Model model, Map<String, String> request, String accessToken) {
        model.addAttribute(USERNAME, request.get(USERNAME));
        model.addAttribute(PHOTO_URL, request.get(PHOTO_URL));
        model.addAttribute(TOKEN, accessToken);
        model.addAttribute(USER_URL, T_ME + request.get(USERNAME));
    }

    private String constructParam(String name, String value) {
        return name.concat("=")
                   .concat(value);
    }
}