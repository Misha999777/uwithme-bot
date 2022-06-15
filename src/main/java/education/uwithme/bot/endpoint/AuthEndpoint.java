package education.uwithme.bot.endpoint;

import static org.keycloak.OAuth2Constants.AUTHORIZATION_CODE;

import java.util.Map;

import education.uwithme.bot.util.AuthUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthEndpoint {

    private final AuthUtility authUtility;

    @GetMapping("/token")
    public String token(Model model, @RequestParam Map<String, String> request) {
        var code = request.get("code");
        authUtility.constructModel(model, code);
        return "login.html";
    }
}
