package education.uwithme.bot.util;

import com.mborodin.uwm.api.bot.TelegramUserData;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.StringJoiner;

@Component
@RequiredArgsConstructor
public class AuthUtility {

    public static final String HMAC_SHA_256 = "HmacSHA256";
    public static final String SHA_256 = "SHA-256";

    @Value("${bot.key}")
    private String botKey;

    @SneakyThrows
    public void checkTelegramData(TelegramUserData telegramUserData) {
        String data = buildVerificationString(telegramUserData);

        String hash = telegramUserData.getHash();
        byte[] key = MessageDigest.getInstance(SHA_256)
                .digest(botKey.getBytes(StandardCharsets.UTF_8));

        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(new SecretKeySpec(key, HMAC_SHA_256));

        var result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        String resultHash = HexFormat.of()
                .formatHex(result);

        if (hash.compareToIgnoreCase(resultHash) != 0) {
            throw new RuntimeException("Telegram data integrity verification failed");
        }
    }

    private String buildVerificationString(TelegramUserData telegramUserData) {
        StringJoiner joiner = new StringJoiner("\n");

        if (telegramUserData.getAuthDate() != null) {
            joiner.add("auth_date=" + telegramUserData.getAuthDate());
        }
        if (telegramUserData.getFirstName() != null) {
            joiner.add("first_name=" + telegramUserData.getFirstName());
        }
        if (telegramUserData.getId() != null) {
            joiner.add("id=" + telegramUserData.getId());
        }
        if (telegramUserData.getLastName() != null) {
            joiner.add("last_name=" + telegramUserData.getLastName());
        }
        if (telegramUserData.getPhotoUrl() != null) {
            joiner.add("photo_url=" + telegramUserData.getPhotoUrl());
        }
        if (telegramUserData.getUsername() != null) {
            joiner.add("username=" + telegramUserData.getUsername());
        }

        return joiner.toString();
    }
}
