package education.uwithme.bot.endpoint;

import com.mborodin.uwm.api.bot.TelegramData;
import com.mborodin.uwm.api.bot.TelegramUserData;
import education.uwithme.bot.telegram.TelegramBot;
import education.uwithme.bot.util.AuthUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/uwm-bot")
@RequiredArgsConstructor
public class AuthEndpoint {

    private final AuthUtility authUtility;
    private final TelegramBot telegramBot;

    @PostMapping("/auth")
    @Secured("ROLE_SERVICE")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void token(@RequestBody TelegramUserData telegramUserData) {
        TelegramData telegramData = telegramUserData.getTelegramData();
        authUtility.checkTelegramData(telegramData);
        telegramBot.onLoginComplete(Long.parseLong(telegramData.getId()), telegramUserData.getUwmUserId());
    }
}
