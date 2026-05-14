package education.uwithme.bot.util;

import com.mborodin.uwm.api.bot.TelegramData;
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

    private static final String TEST_ENV_SUFFIX = "/test";
    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final String SHA_256 = "SHA-256";

    @Value("${bot.key}")
    private String botKey;

    @SneakyThrows
    public void checkTelegramData(TelegramData telegramUserData) {
        String data = buildVerificationString(telegramUserData);

        String hash = telegramUserData.getHash();
        String hashKey = botKey.replace(TEST_ENV_SUFFIX, "");
        byte[] key = MessageDigest.getInstance(SHA_256)
                .digest(hashKey.getBytes(StandardCharsets.UTF_8));

        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(new SecretKeySpec(key, HMAC_SHA_256));

        var result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        String resultHash = HexFormat.of()
                .formatHex(result);

        if (hash.compareToIgnoreCase(resultHash) != 0) {
            throw new RuntimeException("Telegram data integrity verification failed");
        }
    }

    private String buildVerificationString(TelegramData telegramUserData) {
        StringJoiner joiner = new StringJoiner("\n");
        telegramUserData.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("hash"))
                .forEach(entry -> joiner.add(entry.getKey() + "=" + entry.getValue()));

        return joiner.toString();
    }
}
